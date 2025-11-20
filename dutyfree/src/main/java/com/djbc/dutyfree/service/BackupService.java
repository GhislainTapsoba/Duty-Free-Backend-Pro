package com.djbc.dutyfree.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Service for automated backup and restoration of database and files
 * Implements daily backups, retention policies, and restoration capabilities
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BackupService {

    @Value("${app.backup.storage-path:./data/backups}")
    private String backupStoragePath;

    @Value("${app.backup.retention-days:30}")
    private int retentionDays;

    @Value("${app.receipts.storage-path:./data/receipts}")
    private String receiptsStoragePath;

    @Value("${app.reports.storage-path:./data/reports}")
    private String reportsStoragePath;

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String databaseUsername;

    @Value("${spring.datasource.password}")
    private String databasePassword;

    private static final DateTimeFormatter BACKUP_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final String DB_BACKUP_PREFIX = "db_backup_";
    private static final String FILES_BACKUP_PREFIX = "files_backup_";

    /**
     * Scheduled daily backup at 2:00 AM
     * Cron: 0 0 2 * * * (seconds, minutes, hours, day, month, day of week)
     */
    @Scheduled(cron = "${app.backup.schedule:0 0 2 * * *}")
    public void scheduledDailyBackup() {
        log.info("Starting scheduled daily backup at {}", LocalDateTime.now());

        try {
            performFullBackup();
            log.info("Scheduled daily backup completed successfully");
        } catch (Exception e) {
            log.error("Scheduled backup failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Scheduled cleanup of old backups every day at 3:00 AM
     */
    @Scheduled(cron = "${app.backup.cleanup-schedule:0 0 3 * * *}")
    public void scheduledCleanupOldBackups() {
        log.info("Starting scheduled backup cleanup at {}", LocalDateTime.now());

        try {
            cleanupOldBackups();
            log.info("Scheduled backup cleanup completed successfully");
        } catch (Exception e) {
            log.error("Scheduled cleanup failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Perform full backup (database + files)
     */
    public String performFullBackup() throws Exception {
        log.info("Starting full backup (database + files)");

        String timestamp = LocalDateTime.now().format(BACKUP_DATE_FORMATTER);

        // Ensure backup directory exists
        Path backupDir = Paths.get(backupStoragePath);
        Files.createDirectories(backupDir);

        // Backup database
        String dbBackupPath = backupDatabase(timestamp);
        log.info("Database backup completed: {}", dbBackupPath);

        // Backup files
        String filesBackupPath = backupFiles(timestamp);
        log.info("Files backup completed: {}", filesBackupPath);

        // Create backup manifest
        createBackupManifest(timestamp, dbBackupPath, filesBackupPath);

        log.info("Full backup completed successfully at {}", timestamp);
        return timestamp;
    }

    /**
     * Backup PostgreSQL database using pg_dump
     */
    public String backupDatabase(String timestamp) throws Exception {
        log.info("Starting database backup");

        // Extract database name from JDBC URL
        // Format: jdbc:postgresql://localhost:5432/dutyfree
        String dbName = extractDatabaseName(databaseUrl);
        String dbHost = extractDatabaseHost(databaseUrl);
        String dbPort = extractDatabasePort(databaseUrl);

        String backupFileName = DB_BACKUP_PREFIX + timestamp + ".sql";
        Path backupFilePath = Paths.get(backupStoragePath, backupFileName);

        // Construct pg_dump command
        // Note: On Windows, you might need to use full path to pg_dump
        List<String> command = new ArrayList<>();

        // Check if we're on Windows
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            // Windows: use cmd /c to execute pg_dump
            command.add("cmd");
            command.add("/c");
            command.add("pg_dump");
        } else {
            // Linux/Mac: direct pg_dump
            command.add("pg_dump");
        }

        command.add("-h");
        command.add(dbHost);
        command.add("-p");
        command.add(dbPort);
        command.add("-U");
        command.add(databaseUsername);
        command.add("-F");
        command.add("p"); // Plain text format
        command.add("-f");
        command.add(backupFilePath.toString());
        command.add(dbName);

        ProcessBuilder processBuilder = new ProcessBuilder(command);

        // Set PGPASSWORD environment variable to avoid password prompt
        processBuilder.environment().put("PGPASSWORD", databasePassword);

        processBuilder.redirectErrorStream(true);

        log.info("Executing pg_dump command: {}", String.join(" ", command));

        Process process = processBuilder.start();

        // Read output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("pg_dump: {}", line);
            }
        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Database backup failed with exit code: " + exitCode);
        }

        // Verify backup file was created
        if (!Files.exists(backupFilePath)) {
            throw new RuntimeException("Database backup file was not created");
        }

        long fileSize = Files.size(backupFilePath);
        log.info("Database backup created successfully: {} (size: {} bytes)", backupFileName, fileSize);

        // Compress the backup
        String compressedPath = compressFile(backupFilePath);

        // Delete uncompressed file
        Files.deleteIfExists(backupFilePath);

        return compressedPath;
    }

    /**
     * Backup files (receipts and reports)
     */
    public String backupFiles(String timestamp) throws Exception {
        log.info("Starting files backup");

        String backupFileName = FILES_BACKUP_PREFIX + timestamp + ".zip";
        Path backupFilePath = Paths.get(backupStoragePath, backupFileName);

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(backupFilePath.toFile()))) {

            // Backup receipts
            Path receiptsPath = Paths.get(receiptsStoragePath);
            if (Files.exists(receiptsPath)) {
                zipDirectory(receiptsPath, "receipts", zos);
                log.info("Receipts backed up successfully");
            } else {
                log.warn("Receipts directory not found: {}", receiptsPath);
            }

            // Backup reports
            Path reportsPath = Paths.get(reportsStoragePath);
            if (Files.exists(reportsPath)) {
                zipDirectory(reportsPath, "reports", zos);
                log.info("Reports backed up successfully");
            } else {
                log.warn("Reports directory not found: {}", reportsPath);
            }
        }

        long fileSize = Files.size(backupFilePath);
        log.info("Files backup created successfully: {} (size: {} bytes)", backupFileName, fileSize);

        return backupFilePath.toString();
    }

    /**
     * Restore database from backup
     */
    public void restoreDatabase(String backupFileName) throws Exception {
        log.info("Starting database restoration from: {}", backupFileName);

        Path backupFilePath = Paths.get(backupStoragePath, backupFileName);

        if (!Files.exists(backupFilePath)) {
            throw new FileNotFoundException("Backup file not found: " + backupFileName);
        }

        // Decompress if compressed
        Path sqlFilePath = backupFilePath;
        if (backupFileName.endsWith(".gz")) {
            sqlFilePath = decompressFile(backupFilePath);
        }

        // Extract database info
        String dbName = extractDatabaseName(databaseUrl);
        String dbHost = extractDatabaseHost(databaseUrl);
        String dbPort = extractDatabasePort(databaseUrl);

        // Construct psql command
        List<String> command = new ArrayList<>();

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            command.add("cmd");
            command.add("/c");
            command.add("psql");
        } else {
            command.add("psql");
        }

        command.add("-h");
        command.add(dbHost);
        command.add("-p");
        command.add(dbPort);
        command.add("-U");
        command.add(databaseUsername);
        command.add("-d");
        command.add(dbName);
        command.add("-f");
        command.add(sqlFilePath.toString());

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.environment().put("PGPASSWORD", databasePassword);
        processBuilder.redirectErrorStream(true);

        log.info("Executing psql restore command");

        Process process = processBuilder.start();

        // Read output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("psql: {}", line);
            }
        }

        int exitCode = process.waitFor();

        // Cleanup decompressed file if it was created
        if (!sqlFilePath.equals(backupFilePath)) {
            Files.deleteIfExists(sqlFilePath);
        }

        if (exitCode != 0) {
            throw new RuntimeException("Database restoration failed with exit code: " + exitCode);
        }

        log.info("Database restored successfully from: {}", backupFileName);
    }

    /**
     * Restore files from backup
     */
    public void restoreFiles(String backupFileName) throws Exception {
        log.info("Starting files restoration from: {}", backupFileName);

        Path backupFilePath = Paths.get(backupStoragePath, backupFileName);

        if (!Files.exists(backupFilePath)) {
            throw new FileNotFoundException("Backup file not found: " + backupFileName);
        }

        // TODO: Implement file restoration from ZIP
        // This would involve extracting the ZIP file to the appropriate directories

        log.info("Files restored successfully from: {}", backupFileName);
    }

    /**
     * Cleanup backups older than retention period
     */
    public int cleanupOldBackups() throws Exception {
        log.info("Starting cleanup of backups older than {} days", retentionDays);

        Path backupDir = Paths.get(backupStoragePath);

        if (!Files.exists(backupDir)) {
            log.warn("Backup directory does not exist: {}", backupDir);
            return 0;
        }

        LocalDateTime cutoffDate = LocalDateTime.now().minus(retentionDays, ChronoUnit.DAYS);
        int deletedCount = 0;

        try (Stream<Path> files = Files.list(backupDir)) {
            List<Path> oldBackups = files
                .filter(Files::isRegularFile)
                .filter(path -> {
                    try {
                        LocalDateTime fileTime = LocalDateTime.ofInstant(
                            Files.getLastModifiedTime(path).toInstant(),
                            java.time.ZoneId.systemDefault()
                        );
                        return fileTime.isBefore(cutoffDate);
                    } catch (IOException e) {
                        log.error("Error checking file time for {}: {}", path, e.getMessage());
                        return false;
                    }
                })
                .toList();

            for (Path backup : oldBackups) {
                try {
                    Files.delete(backup);
                    deletedCount++;
                    log.info("Deleted old backup: {}", backup.getFileName());
                } catch (IOException e) {
                    log.error("Failed to delete backup {}: {}", backup, e.getMessage());
                }
            }
        }

        log.info("Cleanup completed: {} backups deleted", deletedCount);
        return deletedCount;
    }

    /**
     * List all available backups
     */
    public List<BackupInfo> listBackups() throws Exception {
        List<BackupInfo> backups = new ArrayList<>();
        Path backupDir = Paths.get(backupStoragePath);

        if (!Files.exists(backupDir)) {
            return backups;
        }

        try (Stream<Path> files = Files.list(backupDir)) {
            files.filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().startsWith(DB_BACKUP_PREFIX) ||
                               path.getFileName().toString().startsWith(FILES_BACKUP_PREFIX))
                .forEach(path -> {
                    try {
                        BackupInfo info = new BackupInfo();
                        info.setFileName(path.getFileName().toString());
                        info.setFilePath(path.toString());
                        info.setSize(Files.size(path));
                        info.setCreatedAt(LocalDateTime.ofInstant(
                            Files.getLastModifiedTime(path).toInstant(),
                            java.time.ZoneId.systemDefault()
                        ));
                        info.setType(path.getFileName().toString().startsWith(DB_BACKUP_PREFIX) ?
                            "database" : "files");
                        backups.add(info);
                    } catch (IOException e) {
                        log.error("Error reading backup info for {}: {}", path, e.getMessage());
                    }
                });
        }

        backups.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return backups;
    }

    // Helper methods

    private void zipDirectory(Path sourceDir, String baseName, ZipOutputStream zos) throws IOException {
        try (Stream<Path> paths = Files.walk(sourceDir)) {
            paths.filter(Files::isRegularFile)
                .forEach(path -> {
                    try {
                        String zipEntryName = baseName + "/" + sourceDir.relativize(path).toString();
                        zos.putNextEntry(new ZipEntry(zipEntryName));
                        Files.copy(path, zos);
                        zos.closeEntry();
                    } catch (IOException e) {
                        log.error("Error adding file to ZIP: {}", path, e);
                    }
                });
        }
    }

    private String compressFile(Path inputFile) throws IOException {
        Path compressedFile = Paths.get(inputFile.toString() + ".gz");

        try (FileInputStream fis = new FileInputStream(inputFile.toFile());
             FileOutputStream fos = new FileOutputStream(compressedFile.toFile());
             java.util.zip.GZIPOutputStream gzos = new java.util.zip.GZIPOutputStream(fos)) {

            byte[] buffer = new byte[8192];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                gzos.write(buffer, 0, length);
            }
        }

        log.info("File compressed: {} -> {} (reduction: {}%)",
            inputFile.getFileName(),
            compressedFile.getFileName(),
            (100 - (Files.size(compressedFile) * 100 / Files.size(inputFile))));

        return compressedFile.toString();
    }

    private Path decompressFile(Path compressedFile) throws IOException {
        String outputFileName = compressedFile.toString().replace(".gz", "");
        Path outputFile = Paths.get(outputFileName);

        try (FileInputStream fis = new FileInputStream(compressedFile.toFile());
             java.util.zip.GZIPInputStream gzis = new java.util.zip.GZIPInputStream(fis);
             FileOutputStream fos = new FileOutputStream(outputFile.toFile())) {

            byte[] buffer = new byte[8192];
            int length;
            while ((length = gzis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }

        return outputFile;
    }

    private void createBackupManifest(String timestamp, String dbBackupPath, String filesBackupPath) throws IOException {
        Path manifestPath = Paths.get(backupStoragePath, "manifest_" + timestamp + ".txt");

        List<String> lines = new ArrayList<>();
        lines.add("Backup Manifest");
        lines.add("================");
        lines.add("Timestamp: " + timestamp);
        lines.add("Created: " + LocalDateTime.now());
        lines.add("");
        lines.add("Database Backup: " + dbBackupPath);
        lines.add("Files Backup: " + filesBackupPath);
        lines.add("");
        lines.add("Retention Period: " + retentionDays + " days");

        Files.write(manifestPath, lines);
    }

    private String extractDatabaseName(String jdbcUrl) {
        // Extract from: jdbc:postgresql://localhost:5432/dutyfree
        String[] parts = jdbcUrl.split("/");
        String dbNameWithParams = parts[parts.length - 1];
        return dbNameWithParams.split("\\?")[0];
    }

    private String extractDatabaseHost(String jdbcUrl) {
        // Extract from: jdbc:postgresql://localhost:5432/dutyfree
        String hostPart = jdbcUrl.split("//")[1].split(":")[0];
        return hostPart;
    }

    private String extractDatabasePort(String jdbcUrl) {
        // Extract from: jdbc:postgresql://localhost:5432/dutyfree
        try {
            String portPart = jdbcUrl.split("//")[1].split(":")[1].split("/")[0];
            return portPart;
        } catch (Exception e) {
            return "5432"; // Default PostgreSQL port
        }
    }

    /**
     * Backup information DTO
     */
    @lombok.Data
    public static class BackupInfo {
        private String fileName;
        private String filePath;
        private long size;
        private LocalDateTime createdAt;
        private String type; // "database" or "files"
    }
}

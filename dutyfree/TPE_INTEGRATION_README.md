# Intégration TPE (Terminal de Paiement Électronique) - Documentation

## Vue d'ensemble

Le système Duty Free POS supporte maintenant l'intégration complète des Terminaux de Paiement Électronique (TPE) avec gestion des transactions, des heartbeats, et support multi-fabricants.

## Fonctionnalités

### 1. Gestion des Terminaux

- **CRUD complet** pour les terminaux de paiement
- **Support multi-fabricants**: INGENICO, VERIFONE, PAX, etc.
- **Types de terminaux**: Fixe, Portable, Mobile, PINPAD, Virtuel
- **Types de connexion**: Ethernet, WiFi, Bluetooth, Série, GPRS, Cloud API
- **Statuts en temps réel**: ONLINE, OFFLINE, BUSY, ERROR, MAINTENANCE
- **Heartbeat monitoring**: Suivi de l'état des terminaux

### 2. Types de Paiement Supportés

- **Contactless/NFC**: Paiement sans contact
- **Chip/EMV**: Carte à puce
- **Magnetic Stripe**: Bande magnétique
- **Mobile Payment**: Apple Pay, Google Pay, etc.

### 3. Types de Transactions

- **SALE**: Vente standard
- **REFUND**: Remboursement
- **CANCELLATION**: Annulation
- **PREAUTH**: Pré-autorisation
- **COMPLETION**: Complétion de pré-autorisation
- **VOID**: Annulation du jour
- **REVERSAL**: Contre-passation

### 4. Tracking Complet des Transactions

- **ID de transaction unique**
- **Code d'autorisation**
- **Numéro de référence**
- **Informations carte masquées** (****1234)
- **Temps de réponse** en millisecondes
- **Reçus** (marchand et client)
- **Gestion des erreurs** avec codes et messages

## Architecture

### Entités

#### PaymentTerminal

```java
@Entity
@Table(name = "payment_terminals")
public class PaymentTerminal {
    private Long id;
    private String terminalId;         // Identifiant unique
    private String name;
    private String manufacturer;        // INGENICO, VERIFONE, etc.
    private String model;
    private TerminalType terminalType;  // FIXED, PORTABLE, MOBILE, etc.
    private ConnectionType connectionType; // ETHERNET, WIFI, etc.
    private String ipAddress;
    private Integer port;
    private TerminalStatus status;      // ONLINE, OFFLINE, BUSY, etc.
    private CashRegister cashRegister;  // Caisse associée
    private Boolean supportsContactless;
    private Boolean supportsChip;
    private LocalDateTime lastHeartbeat;
}
```

#### TerminalTransaction

```java
@Entity
@Table(name = "terminal_transactions")
public class TerminalTransaction {
    private Long id;
    private String transactionId;
    private PaymentTerminal terminal;
    private Payment payment;
    private TransactionType transactionType;
    private BigDecimal amount;
    private TransactionStatus status;
    private String cardType;            // VISA, MASTERCARD, etc.
    private String cardNumberMasked;    // ****1234
    private String authorizationCode;
    private String referenceNumber;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Long responseTimeMs;
}
```

### Énumérations

```java
// Types de terminaux
enum TerminalType {
    FIXED,          // Terminal fixe
    PORTABLE,       // Terminal portable
    MOBILE,         // Terminal mobile
    PINPAD,         // PIN pad only
    VIRTUAL         // Terminal virtuel (tests)
}

// Types de connexion
enum ConnectionType {
    ETHERNET,       // Connexion réseau
    WIFI,           // WiFi
    BLUETOOTH,      // Bluetooth
    SERIAL,         // Port série
    GPRS,           // GPRS/3G/4G
    CLOUD_API       // API Cloud
}

// Statuts du terminal
enum TerminalStatus {
    ONLINE,         // En ligne et prêt
    OFFLINE,        // Hors ligne
    BUSY,           // Transaction en cours
    ERROR,          // En erreur
    MAINTENANCE     // En maintenance
}

// Statuts de transaction
enum TransactionStatus {
    PENDING,        // En attente
    PROCESSING,     // En traitement
    APPROVED,       // Approuvée
    DECLINED,       // Refusée
    TIMEOUT,        // Timeout
    ERROR,          // Erreur
    CANCELLED,      // Annulée
    REVERSED        // Contre-passée
}
```

## API Endpoints

### Terminaux

#### Créer un terminal
```http
POST /api/payment-terminals
Authorization: Bearer {token}
Content-Type: application/json

{
  "terminalId": "TPE001",
  "name": "Terminal Principal",
  "manufacturer": "INGENICO",
  "model": "iWL250",
  "terminalType": "PORTABLE",
  "connectionType": "WIFI",
  "ipAddress": "192.168.1.100",
  "port": 8080,
  "merchantId": "MERCHANT001",
  "cashRegisterId": 1,
  "supportsContactless": true,
  "supportsChip": true,
  "supportsMagneticStripe": true,
  "supportsMobilePayment": false
}
```

#### Lister les terminaux disponibles
```http
GET /api/payment-terminals/available?cashRegisterId=1
Authorization: Bearer {token}
```

Response:
```json
[
  {
    "id": 1,
    "terminalId": "TPE001",
    "name": "Terminal Principal",
    "manufacturer": "INGENICO",
    "status": "ONLINE",
    "ready": true,
    "lastHeartbeat": "2025-01-18T10:30:00",
    "secondsSinceLastHeartbeat": 5
  }
]
```

#### Mettre à jour le heartbeat
```http
POST /api/payment-terminals/TPE001/heartbeat?status=ONLINE
Authorization: Bearer {token}
```

### Transactions

#### Traiter un paiement
```http
POST /api/payment-terminals/process-payment
Authorization: Bearer {token}
Content-Type: application/json

{
  "terminalId": 1,
  "paymentId": 100,
  "amount": 50000,
  "currency": "XOF",
  "transactionType": "SALE",
  "requireSignature": false
}
```

Response:
```json
{
  "id": 1,
  "transactionId": "TPE-1705575000000-ABC123DE",
  "terminalId": 1,
  "terminalName": "Terminal Principal",
  "paymentId": 100,
  "transactionType": "SALE",
  "amount": 50000,
  "currency": "XOF",
  "status": "APPROVED",
  "cardType": "VISA",
  "cardNumberMasked": "****1234",
  "authorizationCode": "AUTH-123456",
  "referenceNumber": "550e8400-e29b-41d4-a716-446655440000",
  "responseTimeMs": 1234,
  "pinVerified": true,
  "successful": true,
  "canBeReversed": true
}
```

#### Consulter les transactions
```http
GET /api/payment-terminals/1/transactions
Authorization: Bearer {token}
```

#### Consulter les transactions par période
```http
GET /api/payment-terminals/transactions?startDate=2025-01-01T00:00:00&endDate=2025-01-31T23:59:59
Authorization: Bearer {token}
```

## Intégration avec les Fabricants

### Structure pour Intégration Réelle

L'implémentation actuelle utilise une simulation. Pour intégrer de vrais terminaux, remplacer la méthode `simulateTerminalTransaction()` par l'appel au SDK du fabricant:

#### Exemple INGENICO
```java
// Dans PaymentTerminalService.java
private boolean processIngenicoTransaction(PaymentTerminal terminal, TerminalTransaction transaction) {
    // Utiliser le SDK INGENICO
    IngenicoTerminal ingenicoTerminal = new IngenicoTerminal(
        terminal.getIpAddress(),
        terminal.getPort()
    );

    IngenicoResponse response = ingenicoTerminal.processSale(
        transaction.getAmount(),
        transaction.getCurrency()
    );

    if (response.isApproved()) {
        transaction.setAuthorizationCode(response.getAuthCode());
        transaction.setReferenceNumber(response.getReferenceNumber());
        transaction.setCardType(response.getCardType());
        transaction.setCardNumberMasked(response.getMaskedPan());
        return true;
    }

    transaction.setErrorCode(response.getErrorCode());
    transaction.setErrorMessage(response.getErrorMessage());
    return false;
}
```

#### Exemple VERIFONE
```java
private boolean processVerifoneTransaction(PaymentTerminal terminal, TerminalTransaction transaction) {
    // Utiliser le SDK VERIFONE
    VerifoneTerminal verifone = VerifoneTerminal.connect(
        terminal.getIpAddress(),
        terminal.getPort()
    );

    VerifoneTransactionRequest request = VerifoneTransactionRequest.builder()
        .amount(transaction.getAmount())
        .currency(transaction.getCurrency())
        .transactionType(VerifoneTransactionType.SALE)
        .build();

    VerifoneTransactionResponse response = verifone.process(request);

    // Mapper la réponse vers notre entité
    // ...

    return response.isSuccess();
}
```

### Factory Pattern pour Multi-Fabricants

```java
@Component
public class TerminalProcessorFactory {

    public TerminalProcessor getProcessor(PaymentTerminal terminal) {
        return switch (terminal.getManufacturer().toUpperCase()) {
            case "INGENICO" -> new IngenicoProcessor();
            case "VERIFONE" -> new VerifoneProcessor();
            case "PAX" -> new PaxProcessor();
            default -> new GenericProcessor();
        };
    }
}

public interface TerminalProcessor {
    TerminalTransaction processPayment(PaymentTerminal terminal, BigDecimal amount, String currency);
    void sendHeartbeat(PaymentTerminal terminal);
    boolean isOnline(PaymentTerminal terminal);
}
```

## Configuration

### Application Properties

```yaml
# application.yml
dutyfree:
  payment:
    terminal:
      heartbeat-interval: 30s
      transaction-timeout: 60s
      retry-attempts: 3
      enable-simulation: true  # false pour prod avec vrais terminaux

  manufacturers:
    ingenico:
      api-url: https://api.ingenico.com
      api-key: ${INGENICO_API_KEY}

    verifone:
      api-url: https://api.verifone.com
      api-key: ${VERIFONE_API_KEY}
```

### Database Migration

La migration V13 crée les tables nécessaires:

```sql
-- payment_terminals
-- terminal_transactions
-- + indexes et triggers
```

## Sécurité

### Recommandations

1. **Données sensibles**:
   - Ne JAMAIS stocker le numéro de carte complet
   - Utiliser uniquement le masquage (****1234)
   - Chiffrer les communications avec les terminaux (TLS/SSL)

2. **PCI DSS Compliance**:
   - Audit logs pour toutes les transactions
   - Rotation des clés API
   - Accès restreint aux données de transactions

3. **Authentication**:
   - Terminaux authentifiés par ID + secret
   - Certificats SSL pour connexions sécurisées
   - IP whitelisting pour terminaux fixes

## Monitoring

### Métriques à Surveiller

1. **Disponibilité des terminaux**:
   - Temps écoulé depuis dernier heartbeat
   - Taux de terminaux online vs offline

2. **Performance des transactions**:
   - Temps de réponse moyen
   - Taux d'approbation vs refus
   - Taux de timeout/erreurs

3. **Alertes**:
   - Terminal offline > 5 minutes
   - Taux d'erreur > 5%
   - Temps de réponse > 10 secondes

### Requêtes Utiles

```sql
-- Terminaux hors ligne
SELECT * FROM payment_terminals
WHERE status = 'OFFLINE'
  AND active = true;

-- Taux d'approbation des dernières 24h
SELECT
  COUNT(*) FILTER (WHERE status = 'APPROVED') * 100.0 / COUNT(*) as approval_rate
FROM terminal_transactions
WHERE created_at > NOW() - INTERVAL '24 hours';

-- Temps de réponse moyen par terminal
SELECT
  pt.name,
  AVG(tt.response_time_ms) as avg_response_ms
FROM terminal_transactions tt
JOIN payment_terminals pt ON tt.terminal_id = pt.id
WHERE tt.created_at > NOW() - INTERVAL '7 days'
GROUP BY pt.id, pt.name;
```

## Tests

### Test d'un Terminal Virtuel

```bash
# Créer un terminal de test
curl -X POST http://localhost:8080/api/payment-terminals \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "terminalId": "TEST001",
    "name": "Terminal Test",
    "manufacturer": "VIRTUAL",
    "terminalType": "VIRTUAL",
    "connectionType": "CLOUD_API",
    "supportsContactless": true,
    "supportsChip": true
  }'

# Traiter un paiement test
curl -X POST http://localhost:8080/api/payment-terminals/process-payment \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "terminalId": 1,
    "amount": 1000,
    "currency": "XOF",
    "transactionType": "SALE"
  }'
```

## Dépannage

### Terminal ne répond pas

1. Vérifier la connexion réseau
2. Vérifier le heartbeat: `GET /api/payment-terminals/{id}`
3. Redémarrer le terminal
4. Vérifier les logs d'erreur

### Transaction en timeout

1. Vérifier le statut du terminal (doit être ONLINE)
2. Augmenter le timeout dans la configuration
3. Vérifier la latence réseau
4. Consulter les logs du terminal

### Erreurs fréquentes

| Code | Message | Solution |
|------|---------|----------|
| E001 | Transaction declined | Vérifier solde carte |
| E002 | Terminal offline | Vérifier connexion |
| E003 | Invalid card | Réessayer lecture carte |
| E004 | Timeout | Augmenter timeout config |

## Roadmap

### Court terme
- [ ] Intégration SDK INGENICO
- [ ] Intégration SDK VERIFONE
- [ ] Support multi-devises avancé

### Moyen terme
- [ ] Signature électronique
- [ ] Tokenisation des cartes
- [ ] Réconciliation automatique

### Long terme
- [ ] IA pour détection fraude
- [ ] Support cryptocurrencies
- [ ] Paiement fractionné (split payment)

## Support

Pour toute question:
1. Consulter les logs: `/var/log/dutyfree/terminal.log`
2. Vérifier la documentation du fabricant
3. Contacter le support technique

## Licence

© 2025 DJBC Duty Free. Tous droits réservés.

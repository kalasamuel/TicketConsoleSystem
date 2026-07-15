# Virtual Solutions Call Center Ticket System — Hibernate + MySQL Edition

This is the same CLI application, restructured to persist tickets in MySQL through
Hibernate instead of a tab-delimited text file.

## What changed vs. the original

| Original | This version |
|---|---|
| `FileStorage` reads/writes `data/tickets.txt` | `HibernateTicketRepository` runs SQL through Hibernate |
| `Ticket.toFileLine()` / `fromFileLine()` | Hibernate maps `Ticket` fields to columns via annotations |
| Comments = `List<String>` joined with a `\u001F` delimiter | Comments = a real `Comment` entity, one row per comment |
| `TicketIDGenerator` (AtomicInteger seeded by re-reading the file) | MySQL `AUTO_INCREMENT` assigns the id; `Ticket.assignTicketCode()` formats it as `T000001` right after insert |
| `categoryIndex` (HashMap kept in memory) | A `WHERE category = ?` query — the database is the index |
| No abstraction between `TicketService` and storage | New `TicketRepository` interface + `HibernateTicketRepository` implementation |

`Main.java`, `TicketService`, `ReportService`, `InputHelper`, and `Validator` keep the
same menu flow and prompts as before — the CLI experience is identical. Only how data
gets to disk has changed.

## One-time setup

**1. Install MySQL** if you don't already have it running, and confirm you can log in:

```bash
mysql -u root -p
```

You don't need to create the `ticket_system` database by hand — the JDBC URL in
`hibernate.cfg.xml` includes `createDatabaseIfNotExist=true`, so MySQL will create it
on first connection. You do need a MySQL user with permission to do that.

**2. Edit `src/main/resources/hibernate.cfg.xml`** — set your real username and password:

```xml
<property name="hibernate.connection.username">root</property>
<property name="hibernate.connection.password">your_password_here</property>
```

**3. Build and run** with Maven:

```bash
mvn clean package
java -jar target/ticket-system.jar
```

On first run, `hibernate.hbm2ddl.auto=update` tells Hibernate to create the `tickets`
and `comments` tables itself, based on the `@Entity` classes. You'll see them appear
in MySQL — no `CREATE TABLE` script required.

## Schema Hibernate will generate

```sql
CREATE TABLE tickets (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_code   VARCHAR(10) UNIQUE,
    customer_name VARCHAR(100) NOT NULL,
    phone_number  VARCHAR(20)  NOT NULL,
    category      VARCHAR(30)  NOT NULL,
    priority      VARCHAR(10)  NOT NULL,
    status        VARCHAR(15)  NOT NULL,
    description   VARCHAR(2000),
    creation_date DATE NOT NULL
);

CREATE TABLE comments (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    text       VARCHAR(1000) NOT NULL,
    created_at DATETIME NOT NULL,
    ticket_id  BIGINT NOT NULL,
    FOREIGN KEY (ticket_id) REFERENCES tickets(id)
);
```

## Things worth knowing if you get asked about this in an exam / demo

- **Why `hbm2ddl.auto=update` and not a hand-written schema?** It's convenient while
  the entity design is still moving, but it's not something you'd trust in a real
  production system — it can't handle destructive changes (renaming/removing a column)
  safely. Once the schema is stable, switch it to `validate` and manage schema changes
  with a migration tool (e.g. Flyway or Liquibase) instead.
- **Why session-per-operation instead of one long-lived session?** Each repository
  method opens a `Session`, does one transaction, and closes it. That keeps the
  connection pool healthy and avoids holding a transaction open across user input
  (which, in a CLI app, could be an arbitrarily long pause).
- **Why is `ticketCode` a separate column instead of just using the numeric id?**
  Purely cosmetic/compatibility — it keeps the `T000001` format your examiners already
  saw, without asking MySQL to zero-pad numbers for you.
- **Why is `Comment` a full `@Entity` rather than an `@ElementCollection` of strings?**
  It lets each comment carry its own real `LocalDateTime` and be queried on its own
  (e.g. "all comments left this week" across every ticket) — something a delimited
  string list could never support.

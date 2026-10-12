CREATE TABLE IF NOT EXISTS ledger_source (
  sha256 VARCHAR(64) PRIMARY KEY,
  kind VARCHAR(24) NOT NULL,
  fileName TEXT NOT NULL,
  expectedRows INT NOT NULL,
  importedAt VARCHAR(40) NOT NULL
);
CREATE TABLE IF NOT EXISTS ledger_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  sourceSha VARCHAR(64) NOT NULL,
  kind VARCHAR(24) NOT NULL,
  sheetName VARCHAR(255) NOT NULL,
  rowNumber INT NOT NULL,
  rawJson MEDIUMTEXT NOT NULL,
  mappedJson MEDIUMTEXT NOT NULL,
  targetTable VARCHAR(64),
  targetId INT,
  linkStatus VARCHAR(32) NOT NULL,
  identifier TEXT,
  accountNumber TEXT,
  name TEXT,
  address TEXT,
  street TEXT,
  facilityType TEXT,
  collectedAt TEXT,
  identifierKey VARCHAR(512),
  accountKey VARCHAR(512),
  nameKey VARCHAR(1024),
  streetKey VARCHAR(512),
  addressKey VARCHAR(2048),
  positionKey VARCHAR(2048),
  CONSTRAINT ledger_source_row UNIQUE (sourceSha, sheetName, rowNumber),
  CONSTRAINT ledger_source_fk FOREIGN KEY (sourceSha) REFERENCES ledger_source(sha256)
);
CREATE INDEX IF NOT EXISTS ledger_kind_idx ON ledger_record(kind);
CREATE INDEX IF NOT EXISTS ledger_identifier_idx ON ledger_record(identifierKey);
CREATE INDEX IF NOT EXISTS ledger_account_idx ON ledger_record(accountKey);
CREATE INDEX IF NOT EXISTS ledger_name_idx ON ledger_record(nameKey);
CREATE INDEX IF NOT EXISTS ledger_kind_page_idx ON ledger_record(kind,id);
CREATE INDEX IF NOT EXISTS ledger_status_page_idx ON ledger_record(linkStatus,kind,id);
CREATE INDEX IF NOT EXISTS ledger_source_page_idx ON ledger_record(sourceSha,id);

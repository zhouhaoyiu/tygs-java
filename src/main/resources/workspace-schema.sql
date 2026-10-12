CREATE TABLE IF NOT EXISTS workspace_state (
    module VARCHAR(40) NOT NULL,
    recordId BIGINT NOT NULL,
    archived BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY(module, recordId)
);
CREATE TABLE IF NOT EXISTS workspace_change (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    module VARCHAR(40) NOT NULL,
    recordId BIGINT NOT NULL,
    action VARCHAR(30) NOT NULL,
    actor VARCHAR(200) NOT NULL,
    beforeJson CLOB NOT NULL,
    afterJson CLOB NOT NULL,
    createdAt VARCHAR(40) NOT NULL
);
CREATE INDEX IF NOT EXISTS workspace_change_record_idx ON workspace_change(module, recordId, id);
CREATE TABLE IF NOT EXISTS workspace_request (
    module VARCHAR(40) NOT NULL,
    requestId VARCHAR(80) NOT NULL,
    inputHash VARCHAR(64) NOT NULL,
    recordId BIGINT NOT NULL,
    PRIMARY KEY(module, requestId)
);
CREATE TABLE IF NOT EXISTS workspace_import (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    module VARCHAR(40) NOT NULL,
    sourceSha VARCHAR(64) NOT NULL,
    fileName VARCHAR(500) NOT NULL,
    sheetName VARCHAR(500) NOT NULL,
    rowNumber INT NOT NULL,
    recordId BIGINT NOT NULL,
    rawJson CLOB NOT NULL,
    mappedJson CLOB NOT NULL,
    importedAt VARCHAR(40) NOT NULL,
    UNIQUE(module, sourceSha, sheetName, rowNumber)
);
CREATE TABLE IF NOT EXISTS facility_location (
    targetTable VARCHAR(40) NOT NULL,
    targetId BIGINT NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    crs VARCHAR(20) NOT NULL,
    evidence VARCHAR(2000) NOT NULL,
    actor VARCHAR(200) NOT NULL,
    updatedAt VARCHAR(40) NOT NULL,
    revision BIGINT NOT NULL,
    PRIMARY KEY(targetTable, targetId)
);
CREATE TABLE IF NOT EXISTS facility_geocode (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requestId VARCHAR(80) NOT NULL UNIQUE,
    targetTable VARCHAR(40) NOT NULL,
    targetId BIGINT NOT NULL,
    sourceAddress CLOB NOT NULL,
    queryAddress CLOB NOT NULL,
    decision VARCHAR(30) NOT NULL,
    reason CLOB NOT NULL,
    longitude DOUBLE PRECISION,
    latitude DOUBLE PRECISION,
    resultJson CLOB NOT NULL,
    actor VARCHAR(200) NOT NULL,
    createdAt VARCHAR(40) NOT NULL
);
CREATE INDEX IF NOT EXISTS facility_geocode_record_idx ON facility_geocode(targetTable, targetId, id);

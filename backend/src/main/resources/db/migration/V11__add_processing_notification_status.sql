ALTER TABLE notifications
    DROP CHECK chk_notifications_status;

ALTER TABLE notifications
    ADD CONSTRAINT chk_notifications_status
    CHECK (status IN ('PENDING', 'PROCESSING', 'SENT', 'FAILED', 'CANCELLED'));
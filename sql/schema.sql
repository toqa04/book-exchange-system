
DROP DATABASE IF EXISTS bookexchange;
CREATE DATABASE IF NOT EXISTS bookexchange
 CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
use bookexchange;


START TRANSACTION;

-- USERS
CREATE TABLE IF NOT EXISTS users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    academic_year VARCHAR(20),
    role          VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
    is_blocked    TINYINT(1) NOT NULL DEFAULT 0,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (role IN ('STUDENT','ADMIN'))
) ENGINE=InnoDB;

CREATE INDEX idx_users_email ON users(email);

-- CATEGORIES
CREATE TABLE IF NOT EXISTS categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
) ENGINE=InnoDB;

-- COURSE CODES
CREATE TABLE IF NOT EXISTS course_codes (
    course_id  INT AUTO_INCREMENT PRIMARY KEY,
    code       VARCHAR(20) NOT NULL UNIQUE,
    name       VARCHAR(200),
    department VARCHAR(100)
) ENGINE=InnoDB;

-- LISTINGS
CREATE TABLE IF NOT EXISTS listings (
    listing_id     INT AUTO_INCREMENT PRIMARY KEY,
    user_id        INT NOT NULL,
    listing_type   VARCHAR(20) NOT NULL,
    title          VARCHAR(200) NOT NULL,
    author         VARCHAR(200),
    edition        VARCHAR(50),
    course_code    VARCHAR(20),
    category_id    INT,
    condition_type VARCHAR(20) NOT NULL,
    image_path     VARCHAR(500),
    price          DECIMAL(10,2),
    status         VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    expiry_date    DATE NOT NULL,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                   ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_listings_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_listings_category
        FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE SET NULL,

    CHECK (listing_type IN ('SELL','EXCHANGE')),
    CHECK (condition_type IN ('NEW','LIKE_NEW','USED','DAMAGED')),
    CHECK (status IN ('AVAILABLE','RESERVED','SOLD','EXCHANGED','EXPIRED'))
) ENGINE=InnoDB;

CREATE INDEX idx_listings_status ON listings(status);
CREATE INDEX idx_listings_type   ON listings(listing_type);
CREATE INDEX idx_listings_user   ON listings(user_id);

-- EXCHANGE PROPOSALS
CREATE TABLE IF NOT EXISTS exchange_proposals (
    proposal_id         INT AUTO_INCREMENT PRIMARY KEY,
    listing_id          INT NOT NULL,
    proposed_listing_id INT NOT NULL,
    proposer_id         INT NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    owner_confirmed     TINYINT(1) NOT NULL DEFAULT 0,
    proposer_confirmed  TINYINT(1) NOT NULL DEFAULT 0,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                          ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_proposals_listing
        FOREIGN KEY (listing_id) REFERENCES listings(listing_id) ON DELETE CASCADE,
    CONSTRAINT fk_proposals_proposed_listing
        FOREIGN KEY (proposed_listing_id) REFERENCES listings(listing_id) ON DELETE CASCADE,
    CONSTRAINT fk_proposals_proposer
        FOREIGN KEY (proposer_id) REFERENCES users(user_id) ON DELETE CASCADE,

    CHECK (status IN ('PENDING','ACCEPTED','REJECTED','CANCELLED','COMPLETED'))
) ENGINE=InnoDB;

CREATE INDEX idx_proposals_listing ON exchange_proposals(listing_id);
CREATE INDEX idx_proposals_status  ON exchange_proposals(status);

-- RESERVATIONS
CREATE TABLE IF NOT EXISTS reservations (
    reservation_id INT AUTO_INCREMENT PRIMARY KEY,
    listing_id     INT NOT NULL,
    buyer_id       INT NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                      ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_reservations_listing
        FOREIGN KEY (listing_id) REFERENCES listings(listing_id) ON DELETE CASCADE,
    CONSTRAINT fk_reservations_buyer
        FOREIGN KEY (buyer_id) REFERENCES users(user_id) ON DELETE CASCADE,

    CHECK (status IN ('PENDING','CONFIRMED','CANCELLED'))
) ENGINE=InnoDB;

CREATE INDEX idx_reservations_buyer ON reservations(buyer_id);

-- Enforce only one active reservation per listing
DELIMITER $$

CREATE TRIGGER trg_single_active_reservation
BEFORE INSERT ON reservations
FOR EACH ROW
BEGIN
    IF NEW.status IN ('PENDING','CONFIRMED') AND
       EXISTS (
           SELECT 1 FROM reservations
           WHERE listing_id = NEW.listing_id
             AND status IN ('PENDING','CONFIRMED')
       ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Only one active reservation allowed per listing';
    END IF;
END$$

DELIMITER ;

-- MESSAGES
CREATE TABLE IF NOT EXISTS messages (
    message_id  INT AUTO_INCREMENT PRIMARY KEY,
    sender_id   INT NOT NULL,
    receiver_id INT NOT NULL,
    listing_id  INT,
    subject     VARCHAR(200),
    content     TEXT NOT NULL,
    is_read     TINYINT(1) NOT NULL DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_messages_sender
        FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_receiver
        FOREIGN KEY (receiver_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_listing
        FOREIGN KEY (listing_id) REFERENCES listings(listing_id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE INDEX idx_messages_receiver ON messages(receiver_id);
CREATE INDEX idx_messages_sender   ON messages(sender_id);

-- NOTIFICATIONS
CREATE TABLE IF NOT EXISTS notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL,
    type            VARCHAR(50) NOT NULL,
    title           VARCHAR(200) NOT NULL,
    message         TEXT NOT NULL,
    related_id      INT,
    is_read         TINYINT(1) NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_notifications_user_read ON notifications(user_id, is_read);
CREATE INDEX idx_notifications_created   ON notifications(created_at);

-- SEED DATA
INSERT IGNORE INTO users (name, email, password, role)
VALUES ('UserAdmin', 'useradmin@webeng.com', 'admin2025', 'ADMIN');

INSERT IGNORE INTO categories (name, description) VALUES
('Computer Science', 'Computer Science and IT related books'),
('Mathematics', 'Mathematics and Statistics books'),
('Engineering', 'Engineering textbooks'),
('Business', 'Business and Management books'),
('Science', 'Natural Sciences textbooks'),
('Literature', 'Literature and Language books');

INSERT IGNORE INTO course_codes (code, name, department) VALUES
('CS101', 'Introduction to Computer Science', 'Computer Science'),
('CS201', 'Data Structures', 'Computer Science'),
('MATH101', 'Calculus I', 'Mathematics'),
('ENG101', 'Introduction to Engineering', 'Engineering'),
('BUS101', 'Introduction to Business', 'Business');

COMMIT;


CREATE SCHEMA blib_database;
-- Users Table
CREATE TABLE blib_database.Users (
    userId INT PRIMARY KEY, -- Primary Key
    username VARCHAR(100) NOT NULL, -- Username field
    userPassword VARCHAR(45) NOT NULL,
    role VARCHAR(45) NOT NULL
);



-- Subscribers Table
CREATE TABLE blib_database.Subscribers (
    subscriberId INT PRIMARY KEY, -- Primary Key (same as userId in Users table)
    subscriptionNumber VARCHAR(50) NOT NULL,
    email VARCHAR(255) UNIQUE, -- Email must be unique
    mobilePhoneNumber VARCHAR(15) UNIQUE, -- Phone number must be unique
    accountStatus VARCHAR(20) NOT NULL CHECK (accountStatus IN ('Active', 'Frozen')), -- Restrict account status values
    FOREIGN KEY (subscriberId) REFERENCES Users(userId) -- Foreign Key referencing Users table
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- Add the FreezeLogs Table
CREATE TABLE blib_database.FreezeLogs (
    logId INT AUTO_INCREMENT PRIMARY KEY,
    subscriberId INT NOT NULL,
    freezeEndDate DATE NOT NULL,
    processed BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (subscriberId) REFERENCES Subscribers(subscriberId) ON DELETE CASCADE ON UPDATE CASCADE
);
-- Books Table
CREATE TABLE blib_database.Books (
    bookId INT PRIMARY KEY AUTO_INCREMENT, -- Primary Key
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    availableCopies INT NOT NULL CHECK (availableCopies >= 0), -- Ensure no negative copies
    barcode VARCHAR(255) UNIQUE NOT NULL, -- Barcode must be unique
    nearestReturnDate DATE -- Nearest return date
);

-- BorrowRecords Table
CREATE TABLE blib_database.BorrowRecords (
    recordId INT PRIMARY KEY AUTO_INCREMENT, -- Primary Key
    subscriberId INT NOT NULL, -- Links to Subscribers
    bookId INT NOT NULL, -- Links to Books
    borrowDate DATE NOT NULL,
    dueDate DATE NOT NULL,
    returnDate DATE,
    status VARCHAR(20) NOT NULL CHECK (status IN ('Active', 'Extended', 'Returned')), -- Restrict status values
    FOREIGN KEY (subscriberId) REFERENCES Subscribers(subscriberId) -- Foreign Key referencing Subscribers table
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (bookId) REFERENCES Books(bookId) -- Foreign Key referencing Books tab
);

-- Reservations Table
CREATE TABLE blib_database.Reservations (
    reservationId INT PRIMARY KEY AUTO_INCREMENT,
    bookId INT NOT NULL,
    bookTitle VARCHAR(255) NOT NULL,
    subscriberId INT NOT NULL,
    reservationDate DATE NOT NULL,
    expirationDate DATE,
    priority INT NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('Active', 'Unactive', 'Expired',"Canceled")),
    FOREIGN KEY (bookId) REFERENCES Books(bookId) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (subscriberId) REFERENCES Subscribers(subscriberId) 
);
-- Issues Table
CREATE TABLE blib_database.Issues (
    issueId INT AUTO_INCREMENT PRIMARY KEY, -- Unique identifier for the issue
    subscriberId INT NOT NULL, -- Link to the Subscriber
    description VARCHAR(255) NOT NULL, -- Description of the issue
    dateReported DATE NOT NULL, -- Date the issue was reported
    issueType ENUM('Overdue', 'Lost') NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('Open', 'Resolved')), -- Restrict status values
    FOREIGN KEY (subscriberId) REFERENCES Subscribers(subscriberId) ON DELETE CASCADE ON UPDATE CASCADE
);
-- messages table
CREATE TABLE blib_database.Messages (
    messageId INT AUTO_INCREMENT PRIMARY KEY, -- Unique identifier for the message
    userId INT NOT NULL, -- ID of the user (Subscriber or Librarian)
    content VARCHAR(500) NOT NULL, -- Message content
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, -- Time the message was created
    type ENUM('Borrow', 'Reservation', 'Return', 'Due Reminder', 'Pickup Reminder','ExtensionNotification','AccountStatus') NOT NULL, -- Type of message
    FOREIGN KEY (userId) REFERENCES Users(userId) ON DELETE CASCADE ON UPDATE CASCADE
);
-- reports table
CREATE TABLE blib_database.Reports (
    reportId INT AUTO_INCREMENT PRIMARY KEY,  -- Unique identifier for each report entry
    reportType ENUM('BorrowingTimes', 'SubscriptionStatus') NOT NULL, -- Type of report
    reportMonth DATE NOT NULL,  -- The month the report corresponds to
    category VARCHAR(50) NOT NULL,  -- Category (e.g., "1–7 days", "Overdue", "Active", "Frozen")
    value INT NOT NULL,  -- Count of entries for this category
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- When the report was generated
);


-- Set DELIMITER for Triggers and Events
DELIMITER //

DELIMITER //
-- a trigger to generate a SubscriptionNumber upon insert
CREATE TRIGGER blib_database.GenerateSequentialSubscriptionNumber
BEFORE INSERT ON Subscribers
FOR EACH ROW
BEGIN
    DECLARE nextSubNumber INT;
    
    -- Get the current maximum subscriptionNumber
    SELECT COUNT(*) + 1 INTO nextSubNumber
    FROM Subscribers;

    -- Generate the new subscriptionNumber
    SET NEW.subscriptionNumber = CONCAT('SUB', LPAD(nextSubNumber, 4, '0'));
END;
//


-- Event: Check Reservation Expiration
DELIMITER //

CREATE EVENT blib_database.CheckReservationExpiration
ON SCHEDULE EVERY 1 DAY
DO
BEGIN
       CALL blib_database.CheckReservationExpirationProc();
END //


DELIMITER //
-- Procedure to Check Reservation Expiration
CREATE PROCEDURE blib_database.CheckReservationExpirationProc()
BEGIN
 -- Step 1: Expire reservations whose expiration date has passed
    UPDATE Reservations
    SET status = 'Expired'
    WHERE expirationDate IS NOT NULL AND expirationDate < CURDATE() AND status = 'Active';

    -- Step 2: Increment availableCopies for books where expired reservations had expiration dates
    UPDATE Books b
    SET b.availableCopies = b.availableCopies + 1
    WHERE b.bookId IN (
        SELECT r.bookId
        FROM Reservations r
        WHERE r.expirationDate IS NOT NULL AND r.expirationDate < CURDATE() AND r.status = 'Expired'
    );

    -- Step 3: Assign the next reservation in the queue if a copy is available
    UPDATE Reservations r
    JOIN (
        SELECT MIN(reservationId) AS minReservationId, bookId
        FROM Reservations
        WHERE status = 'Active' AND expirationDate IS NULL
        GROUP BY bookId
    ) nextReservation
    ON r.reservationId = nextReservation.minReservationId
    SET r.expirationDate = DATE_ADD(CURDATE(), INTERVAL 2 DAY)
    WHERE (SELECT b.availableCopies FROM Books b WHERE b.bookId = r.bookId) > 0;

    -- Step 4: Decrement availableCopies after assigning a copy to the next reservation
    UPDATE Books b
    SET b.availableCopies = b.availableCopies - 1
    WHERE b.bookId IN (
        SELECT r.bookId
        FROM Reservations r
        WHERE r.expirationDate = DATE_ADD(CURDATE(), INTERVAL 2 DAY) -- Assigned in Step 3
          AND r.status = 'Active'
    );

END//

-- Trigger: Update Reservation on Return
CREATE TRIGGER blib_database.UpdateReservationOnReturn
AFTER UPDATE ON BorrowRecords
FOR EACH ROW
BEGIN
    IF NEW.returnDate IS NOT NULL THEN
        -- Check the current available copies
        SET @availableCopies = (
            SELECT availableCopies
            FROM Books
            WHERE bookId = NEW.bookId
        );

        -- Increase available copies temporarily since a book has been returned
        UPDATE Books
        SET availableCopies = availableCopies + 1
        WHERE bookId = NEW.bookId;

        -- Find the next reservation in the queue
        SET @nextReservationId = (
            SELECT reservationId
            FROM Reservations
            WHERE bookId = NEW.bookId AND status = 'Active' AND expirationDate IS NULL
            ORDER BY priority ASC
            LIMIT 1
        );

        -- If a reservation exists and copies are available, assign it
        IF @nextReservationId IS NOT NULL THEN
            UPDATE Reservations
            SET expirationDate = DATE_ADD(CURDATE(), INTERVAL 2 DAY)
            WHERE reservationId = @nextReservationId;

            -- Since the book is now reserved, decrease available copies back
            UPDATE Books
            SET availableCopies = availableCopies - 1
            WHERE bookId = NEW.bookId;
        END IF;

        -- Update the nearest return date for the book
        UPDATE Books
        SET nearestReturnDate = (
            SELECT MIN(dueDate)
            FROM BorrowRecords
            WHERE bookId = NEW.bookId AND status IN ('Active', 'Extended')
        )
        WHERE bookId = NEW.bookId;
    END IF;
END;



//

-- Trigger: Assign Priority Before Insert for reservations
CREATE TRIGGER blib_database.AssignPriorityBeforeInsert
BEFORE INSERT ON Reservations
FOR EACH ROW
BEGIN
    DECLARE maxPriority INT;
    
    -- Find the current maximum priority for the given bookId
    SELECT IFNULL(MAX(priority), 0) INTO maxPriority
    FROM Reservations
    WHERE bookId = NEW.bookId AND status = 'Active';
    
    -- Set the new priority to maxPriority + 1
    SET NEW.priority = maxPriority + 1;
END;
//
-- Set freezeEndDate 30 days after freezing'
DELIMITER //
CREATE TRIGGER blib_database.LogFreezeAction
AFTER UPDATE ON Subscribers
FOR EACH ROW
BEGIN
    -- Log the freeze action in the FreezeLogs table when the account status changes to 'Frozen'
    IF NEW.accountStatus = 'Frozen' AND OLD.accountStatus != 'Frozen' THEN
        INSERT INTO FreezeLogs (subscriberId, freezeEndDate) 
        VALUES (NEW.subscriberId, DATE_ADD(CURDATE(), INTERVAL 30 DAY));
    END IF;
END //

 -- an event to check if the duedate for a borrowed book has passed , freeze after 7 days past due date
DELIMITER //
CREATE EVENT blib_database.CheckOverdueReturns
ON SCHEDULE EVERY 1 DAY
DO
BEGIN
 CALL  blib_database.CheckOverdueReturnsProc();

END //

DELIMITER //
 -- an procedue to check if the duedate for a borrowed book has passed , freeze after 7 days past due date
CREATE PROCEDURE blib_database.CheckOverdueReturnsProc()
BEGIN
-- Update Subscribers' accountStatus to 'Frozen' based on overdue borrow records
    UPDATE Subscribers s
    JOIN BorrowRecords b ON s.subscriberId = b.subscriberId
    SET s.accountStatus = 'Frozen'
    WHERE b.status IN ('Active', 'Extended')
      AND DATEDIFF(CURDATE(), b.dueDate) >= 7
      AND s.accountStatus != 'Frozen';
END//

DELIMITER //

CREATE EVENT blib_database.ReportOverdueIssues
ON SCHEDULE EVERY 1 DAY
DO
BEGIN
    CALL blib_database.ReportOverdueIssuesProc();
END //

DELIMITER //
CREATE PROCEDURE blib_database.ReportOverdueIssuesProc()
BEGIN
-- Insert a new "Overdue" issue if one doesn't already exist for the subscriber
    INSERT INTO Issues (subscriberId, description, dateReported, status, issueType)
    SELECT DISTINCT b.subscriberId,
           CONCAT('Overdue borrow record for book ID ', b.bookId),
           CURDATE(),
           'Open',
           'Overdue'
    FROM BorrowRecords b
    WHERE DATEDIFF(CURDATE(), b.dueDate) >= 1
      AND b.status IN ('Active', 'Extended')
      AND NOT EXISTS (
          SELECT 1
          FROM Issues i
          WHERE i.subscriberId = b.subscriberId
            AND i.issueType = 'Overdue'
            AND i.status = 'Open'
      )
      AND NOT EXISTS (
          SELECT 1
          FROM Issues i
          WHERE i.description LIKE CONCAT('Book ID ', b.bookId, ' reported lost')
            AND i.status = 'Open'
      );
END//


-- an event to proccess FreezeLogs ( if finished or ongoing)
DELIMITER //
CREATE EVENT blib_database.ProcessFreezeLogs
ON SCHEDULE EVERY 1 DAY
DO
BEGIN
   CALL  blib_database.ProcessFreezeLogsProc();
END //

DELIMITER //

CREATE PROCEDURE blib_database.ProcessFreezeLogsProc()
BEGIN
 -- Mark freeze logs as processed only after the subscriber's status has been updated to "Active"
    UPDATE FreezeLogs
    SET processed = TRUE
    WHERE logId IN (
        SELECT fl.logId
        FROM FreezeLogs fl
        JOIN Subscribers s ON fl.subscriberId = s.subscriberId
        WHERE s.accountStatus = 'Active' AND fl.processed = FALSE
    );
END//

-- Unfreeze subscribers after 30 days
DELIMITER //
CREATE EVENT blib_database.UnfreezeAccounts
ON SCHEDULE EVERY 1 DAY
DO
BEGIN
   CALL blib_database.UnfreezeAccountsProc();
END //

DELIMITER //
CREATE PROCEDURE blib_database.UnfreezeAccountsProc()
BEGIN
 -- Unfreeze subscribers whose freeze period has ended
    UPDATE Subscribers s
    JOIN FreezeLogs fl ON s.subscriberId = fl.subscriberId
    SET s.accountStatus = 'Active'
    WHERE fl.freezeEndDate <= CURDATE()
      AND fl.processed = FALSE;

    -- Mark the corresponding logs as processed
    UPDATE FreezeLogs
    SET processed = TRUE
    WHERE freezeEndDate <= CURDATE()
      AND processed = FALSE;
END//

-- an event to send a message 1 day before overdue date
DELIMITER //
CREATE EVENT blib_database.CreateDueReminderMessages
ON SCHEDULE EVERY 1 DAY
DO
BEGIN
    CALL blib_database.CreateDueReminderMessagesProc();
END;//

CREATE PROCEDURE blib_database.CreateDueReminderMessagesProc()
BEGIN
INSERT INTO Messages (userId, content, type)
    SELECT b.subscriberId,
           CONCAT('Reminder: The book "', bo.title, '" is due tomorrow.'),
           'Due Reminder'
    FROM BorrowRecords b
    JOIN Books bo ON b.bookId = bo.bookId
    WHERE DATEDIFF(b.dueDate, CURDATE()) = 1
      AND b.status IN ('Active', 'Extended');
END//



-- create a message after a reservation is assigned a book
DELIMITER //
CREATE TRIGGER blib_database.CreatePickupReminder
AFTER UPDATE ON Reservations
FOR EACH ROW
BEGIN
    IF NEW.expirationDate IS NOT NULL AND OLD.expirationDate IS NULL THEN
        INSERT INTO Messages (userId, content, type)
        SELECT NEW.subscriberId,
               CONCAT('Reminder: You have 2 days to pick up the book "', bo.title, '".'),
               'Pickup Reminder'
        FROM Books bo
        WHERE bo.bookId = NEW.bookId;
    END IF;
END;//

DELIMITER //

CREATE TRIGGER blib_database.UpdateFreezeLogOnActivation
AFTER UPDATE ON Subscribers
FOR EACH ROW
BEGIN
    -- Check if the account status changed to 'Active'
    IF NEW.accountStatus = 'Active' AND OLD.accountStatus != 'Active' THEN
        -- Update the corresponding FreezeLogs to mark them as processed
        UPDATE FreezeLogs
        SET processed = TRUE
        WHERE subscriberId = NEW.subscriberId
          AND processed = FALSE;
    END IF;
END;

//


DELIMITER //
-- a trigger to insert a message whenever the status of a subscriber changes
CREATE TRIGGER blib_database.SubscriberStatusChangeMessage
AFTER UPDATE ON Subscribers
FOR EACH ROW
BEGIN
    -- Check if the account status has changed
    IF NEW.accountStatus != OLD.accountStatus THEN
        -- Create a message when the status changes to "Frozen"
        IF NEW.accountStatus = 'Frozen' THEN
            INSERT INTO Messages (userId, content, type, timestamp)
            VALUES (
                NEW.subscriberId,
                CONCAT('Your account status has changed to "Frozen". Please resolve any pending issues to reactivate your account.'),
                'AccountStatus',
                NOW()
            );
        -- Create a message when the status changes to "Active"
        ELSEIF NEW.accountStatus = 'Active' THEN
            INSERT INTO Messages (userId, content, type, timestamp)
            VALUES (
                NEW.subscriberId,
                CONCAT('Your account status has been reactivated. You can now access your account services.'),
                'AccountStatus',
                NOW()
            );
        END IF;
    END IF;
END //


DELIMITER //
-- an event to generete reports at the end of every month
CREATE EVENT blib_database.AutoGenerateReports
ON SCHEDULE EVERY 1 MONTH
STARTS TIMESTAMP(CURRENT_DATE + INTERVAL 1 MONTH - INTERVAL DAY(CURRENT_DATE)-1 DAY)
DO
BEGIN
    -- Generate Borrowing Times Report
    CALL blib_database.GenerateBorrowingTimesReport(DATE_SUB(CURDATE(), INTERVAL 1 MONTH));

    -- Generate Subscription Status Report
    CALL blib_database.GenerateSubscriptionStatusReport(DATE_SUB(CURDATE(), INTERVAL 1 MONTH));
END;//


-- a procedure to generate borrowing time report
DELIMITER //
CREATE PROCEDURE blib_database.GenerateBorrowingTimesReport(IN reportMonth DATE)
BEGIN
    DECLARE startDate DATE;
    DECLARE endDate DATE;

    SET startDate = reportMonth;
    SET endDate = LAST_DAY(reportMonth);

    -- Borrowing within 1–7 days
    INSERT INTO Reports (reportType, reportMonth, category, value)
    SELECT 'BorrowingTimes', startDate, '1–7 days', COUNT(*)
    FROM BorrowRecords
    WHERE borrowDate BETWEEN startDate AND endDate
      AND DATEDIFF(IFNULL(returnDate, CURDATE()), borrowDate) BETWEEN 1 AND 7
    ON DUPLICATE KEY UPDATE value = VALUES(value);

    -- Borrowing within 8–14 days
    INSERT INTO Reports (reportType, reportMonth, category, value)
    SELECT 'BorrowingTimes', startDate, '8–14 days', COUNT(*)
    FROM BorrowRecords
    WHERE borrowDate BETWEEN startDate AND endDate
      AND DATEDIFF(IFNULL(returnDate, CURDATE()), borrowDate) BETWEEN 8 AND 14
    ON DUPLICATE KEY UPDATE value = VALUES(value);

    -- Overdue borrowing
    INSERT INTO Reports (reportType, reportMonth, category, value)
    SELECT 'BorrowingTimes', startDate, 'Overdue', COUNT(*)
    FROM BorrowRecords
    WHERE borrowDate BETWEEN startDate AND endDate
      AND returnDate IS NULL
      AND DATEDIFF(CURDATE(), dueDate) > 0
    ON DUPLICATE KEY UPDATE value = VALUES(value);

    -- Extended borrowing
    INSERT INTO Reports (reportType, reportMonth, category, value)
    SELECT 'BorrowingTimes', startDate, 'Extended Borrowing', COUNT(*)
    FROM BorrowRecords
    WHERE borrowDate BETWEEN startDate AND endDate
      AND DATEDIFF(dueDate, borrowDate) > 14 -- Indicates extended borrowing period
      AND (returnDate <= dueDate OR returnDate IS NULL) -- Includes returned and unreturned books within the due date
    ON DUPLICATE KEY UPDATE value = VALUES(value);
END;
//

DELIMITER //
CREATE PROCEDURE blib_database.GenerateSubscriptionStatusReport(IN reportMonth DATE)
BEGIN
    DECLARE startDate DATE;

    SET startDate = reportMonth;

    -- Insert data dynamically for Subscription Status
    INSERT INTO Reports (reportType, reportMonth, category, value)
    SELECT 'SubscriptionStatus', startDate, accountStatus, COUNT(*)
    FROM Subscribers
    GROUP BY accountStatus
    ON DUPLICATE KEY UPDATE value = VALUES(value);
END;
//



-- Reset DELIMITER
DELIMITER ;


-- insert data for an imaginry data base for testing purposes 
INSERT INTO blib_database.Users (userId, username, userPassword, role) VALUES
(1, 'Librarian1', 'admin1', 'Librarian'),
(2, 'Sub1', 'password1', 'Subscriber'),
(3, 'Sub2', 'password2', 'Subscriber'),
(4, 'Sub3', 'password3', 'Subscriber'),
(5, 'Sub4', 'password4', 'Subscriber'),
(6, 'Librarian2', 'admin2', 'Librarian'),
(7, 'Sub6', 'password6', 'Subscriber'),
(8, 'Sub5', 'password6', 'Subscriber');

INSERT INTO blib_database.Subscribers (subscriberId, subscriptionNumber, email, mobilePhoneNumber, accountStatus) VALUES
(2, 'SUB0001', 'Sub1@example.com', '1234567890', 'Active'),
(3, 'SUB0002', 'Sub2@example.com', '1234567892', 'Frozen'),
(4, 'SUB0003', 'Sub3@example.com', '1234567893', 'Active'),
(5, 'SUB0004', 'Sub4@example.com', '1234567894', 'Active'),
(7, 'SUB0006', 'Sub6@example.com', '1234567896', 'Active'),
(8, 'SUB0005', 'Sub5@example.com', '1234567895', 'Frozen');

INSERT INTO blib_database.Books (bookId, title, author, subject, description, location, availableCopies, barcode, nearestReturnDate) VALUES
(1, 'Book1', 'Author1', 'Fantasy', 'Learn about space', 'Shelf A-1', 4, 'BAR001', '2025-02-10'),
(2, 'Book2', 'Author2', 'Sport', 'Learn about fit', 'Shelf B-1', 4, 'BAR002', NULL),
(3, 'Book3', 'Author3', 'Adventure', 'Learn about woods', 'Shelf C-1', 2, 'BAR003', NULL),
(4, 'Book4', 'Author4', 'Food', 'Learn about taste', 'Shelf D-1', 0, 'BAR004', '2025-01-17'),
(5, 'Book5', 'Author5', 'Engineering', 'Learn how to think', 'Shelf E-1', 6, 'BAR005', '2025-01-10'),
(6, 'Book6', 'Author6', 'Sport', 'Learn about football', 'Shelf B-2', 3, 'BAR006', '2025-01-10');


INSERT INTO blib_database.BorrowRecords (recordId, subscriberId, bookId, borrowDate, dueDate, returnDate, status) VALUES
(1, 8, 1, '2024-12-27', '2025-01-10', NULL, 'Active'),
(2, 2, 2, '2024-12-27', '2025-01-15', '2025-01-14', 'Returned'),
(3, 2, 3, '2024-12-27', '2025-01-10', '2025-01-08', 'Returned'),
(4, 2, 4, '2024-12-27', '2025-01-17', '2025-01-16', 'Returned'),
(5, 2, 5, '2024-12-27', '2025-01-10', '2024-12-27', 'Returned'),
(6, 3, 6, '2024-12-27', '2025-01-10', '2024-12-27', 'Returned'),
(7, 3, 4, '2024-12-27', '2025-01-17', NULL, 'Extended');


INSERT INTO blib_database.FreezeLogs (logId, subscriberId, freezeEndDate, processed) VALUES
(1, 8, '2025-02-16', 0),
(2, 3, '2025-02-23', 0);



INSERT INTO blib_database.Issues (issueId, subscriberId, description, dateReported, issueType, status) VALUES
(1, 2, 'Book ID 3 reported lost', '2025-01-05', 'Lost', 'Resolved'),
(6, 8, 'Overdue borrow record for book ID 1', '2025-01-11', 'Overdue', 'Open'),
(7, 3, 'Overdue borrow record for book ID 4', '2025-01-16', 'Overdue', 'Open');


INSERT INTO blib_database.Reservations (reservationId, bookId, bookTitle, subscriberId, reservationDate, expirationDate, priority, status) VALUES
(1, 4, 'Book4', 2, '2025-01-12', NULL, 1, 'Active'),
(2, 4, 'Book4', 5, '2025-01-14', NULL, 2, 'Active'),
(3, 4, 'Book4', 7, '2025-01-15', NULL, 3, 'Active');


INSERT INTO blib_database.Messages (messageId, userId, content, timestamp, type) VALUES
(64, 8, 'You have successfully borrowed the book ''Book1''. It is due on 2025-01-10.', '2024-12-27 10:00:00', 'Borrow'),
(65, 8, 'Reminder: The book ''Book1'' is due tomorrow.', '2025-01-09 10:00:00', 'Due Reminder'),
(66, 8, 'You have successfully borrowed the book ''Book2''. It is due on 2025-01-15.', '2024-12-27 10:30:00', 'Borrow'),
(67, 8, 'Reminder: The book ''Book2'' is due tomorrow.', '2025-01-14 10:00:00', 'Due Reminder'),
(68, 2, 'You have successfully returned the book ''Book2''.', '2025-01-14 11:00:00', 'Return'),
(69, 2, 'Subscribers SUB0002Extension request for (ID: 2) for book ''Book2'' has been approved.', '2024-12-28 12:00:00', 'ExtensionNotification'),
(70, 2, 'You have successfully borrowed the book ''Book3''. It is due on 2025-01-10.', '2024-12-27 11:00:00', 'Borrow'),
(71, 2, 'Reminder: The book ''Book3'' is due tomorrow.', '2025-01-09 10:00:00', 'Due Reminder'),
(72, 2, 'You have successfully returned the book ''Book3''.', '2025-01-08 15:00:00', 'Return'),
(73, 2, 'You have successfully borrowed the book ''Book4''. It is due on 2025-01-17.', '2024-12-27 12:00:00', 'Borrow'),
(74, 2, 'Reminder: The book ''Book4'' is due tomorrow.', '2025-01-16 10:00:00', 'Due Reminder'),
(75, 2, 'You have successfully returned the book ''Book4''.', '2025-01-16 14:00:00', 'Return'),
(76, 2, 'Subscribers SUB0002Extension request for (ID: 4) for book ''Book4'' has been approved.', '2024-12-28 11:00:00', 'ExtensionNotification'),
(77, 5, 'You have successfully borrowed the book ''Book5''. It is due on 2025-01-10.', '2024-12-27 13:00:00', 'Borrow'),
(78, 5, 'Reminder: The book ''Book5'' is due tomorrow.', '2025-01-09 10:00:00', 'Due Reminder'),
(79, 5, 'You have successfully returned the book ''Book5''.', '2025-01-27 15:00:00', 'Return'),
(80, 3, 'You have successfully borrowed the book ''Book6''. It is due on 2025-01-10.', '2024-12-27 14:00:00', 'Borrow'),
(81, 3, 'Reminder: The book ''Book6'' is due tomorrow.', '2025-01-09 10:00:00', 'Due Reminder'),
(82, 3, 'You have successfully returned the book ''Book6''.', '2025-01-27 18:00:00', 'Return'),
(83, 7, 'You have successfully borrowed the book ''Book7''. It is due on 2025-01-17.', '2024-12-27 15:00:00', 'Borrow'),
(84, 7, 'Reminder: The book ''Book7'' is due tomorrow.', '2025-01-16 10:00:00', 'Due Reminder'),
(85, 7, 'Subscribers SUB0007Extension request for (ID: 7) for book ''Book7'' has been approved.', '2024-12-28 12:00:00', 'ExtensionNotification'),
(86, 2, 'Your account status has changed to Frozen. Please resolve any pending issues to reactivate your account.', '2025-01-17 10:00:00', 'AccountStatus'),
(87, 2, 'Your account status has been reactivated. You can now access your account services.', '2025-01-24 10:00:00', 'AccountStatus');

INSERT INTO blib_database.Reports (reportId, reportType, reportMonth, category, value, createdAt) VALUES
(1, 'BorrowingTimes', '2024-12-01', '1–7 days', 0, '2025-01-28 01:06:24'),
(2, 'BorrowingTimes', '2024-12-01', '8–14 days', 1, '2025-01-28 01:06:24'),
(3, 'BorrowingTimes', '2024-12-01', 'Overdue', 2, '2025-01-28 01:06:24'),
(4, 'BorrowingTimes', '2024-12-01', 'Extended Borrow', 3, '2025-01-28 01:06:24'),
(5, 'SubscriptionStatus', '2024-12-01', 'Active', 4, '2025-01-28 01:07:41'),
(6, 'SubscriptionStatus', '2024-12-01', 'Frozen', 2, '2025-01-28 01:07:41'),
(7, 'BorrowingTimes', '2025-03-01', '1–7 days', 45, '2025-01-28 01:09:37'),
(8, 'BorrowingTimes', '2025-03-01', '8–14 days', 30, '2025-01-28 01:09:37'),
(9, 'BorrowingTimes', '2025-03-01', 'Overdue', 8, '2025-01-28 01:09:37'),
(10, 'BorrowingTimes', '2025-03-01', 'Extended Borrow', 12, '2025-01-28 01:09:37'),
(11, 'SubscriptionStatus', '2025-03-01', 'Active', 52, '2025-01-28 01:09:37'),
(12, 'SubscriptionStatus', '2025-03-01', 'Frozen', 6, '2025-01-28 01:09:37');




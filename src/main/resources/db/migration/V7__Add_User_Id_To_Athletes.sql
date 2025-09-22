-- Add userId column to athletes table to link with users for authentication
ALTER TABLE athletes ADD COLUMN user_id BIGINT UNIQUE;

-- Add foreign key constraint
ALTER TABLE athletes ADD CONSTRAINT fk_athletes_user_id FOREIGN KEY (user_id) REFERENCES users(id);

-- Link existing test athletes to their corresponding users
-- Alex Jones (athlete user id 5) -> athlete id 1
UPDATE athletes SET user_id = 5 WHERE first_name = 'Alex' AND last_name = 'Jones';

-- Jordan Brown (athlete user id 6) -> athlete id 2  
UPDATE athletes SET user_id = 6 WHERE first_name = 'Jordan' AND last_name = 'Brown';

-- Taylor Davis (athlete user id 7) -> athlete id 3
UPDATE athletes SET user_id = 7 WHERE first_name = 'Taylor' AND last_name = 'Davis';

-- Casey Wilson (athlete user id 8) -> athlete id 4
UPDATE athletes SET user_id = 8 WHERE first_name = 'Casey' AND last_name = 'Wilson';

-- Create index for performance
CREATE INDEX idx_athletes_user_id ON athletes(user_id);
DROP TABLE IF EXISTS `bank_account`;

CREATE TABLE `bank_account` (
  `id` int NOT NULL AUTO_INCREMENT,
  `balance` decimal(17,2) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
INSERT INTO bank_account(balance)values(0.00);


DROP TABLE IF EXISTS `transactions`;

CREATE TABLE `transactions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `transaction_type` varchar(255) DEFAULT NULL,
  `reference` varchar(255) DEFAULT NULL,
  `amount` decimal(17,2) DEFAULT NULL,
  `creation_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DELIMITER ;;
CREATE PROCEDURE `make_transaction`(IN trans_type varchar(45),IN v_amount decimal(17,2),IN trans_id varchar(255) )
exitSub:BEGIN

        if v_amount < 0 then select 'negative amounts not allowed' as description; LEAVE exitSub; end if;
        if not exists(select `reference` from transactions where `reference`=trans_id)	then
			insert into transactions (transaction_type,amount,`reference`)
            values(trans_type,v_amount,trans_id);
            update bank_account set balance = if(trans_type='DEPOSIT',balance+v_amount,balance-v_amount);
            select 'success' as description;
            LEAVE exitSub;
        end if;
        select 'dupicate transaction' as description;

END ;;
DELIMITER ;


DROP TABLE IF EXISTS `bank_account`;

CREATE TABLE `bank_account` (
  `id` int NOT NULL AUTO_INCREMENT,
  `balance` decimal(17,2) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


DROP TABLE IF EXISTS `transactions`;

CREATE TABLE `transactions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `transaction_type` varchar(255) DEFAULT NULL,
  `reference` varchar(255) DEFAULT NULL,
  `amount` decimal(17,2) DEFAULT NULL,
  `creation_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `error_log`;

CREATE TABLE `error_log` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `date_in` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `corrected_by` varchar(255) NOT NULL DEFAULT '',
  `proc_name` varchar(50) NOT NULL DEFAULT '',
  `node_id` int NOT NULL DEFAULT '-1',
  `err_no` int NOT NULL DEFAULT '0',
  `err_text` varchar(255) DEFAULT '',
  `row_no` int NOT NULL DEFAULT '0',
  `value1` varchar(255) DEFAULT '',
  `value2` varchar(255) DEFAULT '',
  `text1` varchar(255) DEFAULT '',
  `text2` varchar(255) DEFAULT '',
  `long_text` mediumtext,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `err_log_time_idx` (`date_in`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;


$$

CREATE PROCEDURE `make_transaction`(IN trans_type varchar(45),IN v_amount decimal(17,2),IN trans_id varchar(255) )
exitSub:BEGIN
 
		DECLARE EXIT HANDLER for SQLEXCEPTION
		BEGIN
		  DECLARE errNom INT UNSIGNED DEFAULT 0;
		  DECLARE errText VARCHAR (50);
		  GET DIAGNOSTICS CONDITION 1 errText = MESSAGE_TEXT, errNom = MYSQL_ERRNO;
		  INSERT INTO error_log(proc_name,err_no,err_text,row_no,value1,long_text)
			   VALUES ('make_transaction',errNom,errText,rowNo,concat('trans_id:',trans_id),'EXCEPTION');
		END;
        if v_amount < 0 then select 'negative amounts not allowed' as description; LEAVE exitSub; end if;
        if not exists(select `reference` from transactions where `reference`=trans_id)	then
			insert into transactions (transaction_type,amount,`reference`)
            values(trans_type,v_amount,trans_id);
            update bank_account set balance = if(trans_type='DEPOSIT',balance+v_amount,balance-v_amount);
            select 'success' as description;
            LEAVE exitSub;
        end if;
        select 'dupicate transaction' as description;

END $$$

INSERT INTO bank_account(balance)values(0.00);

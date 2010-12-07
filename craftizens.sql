CREATE TABLE  `minecraft`.`craftizens` (
  `npc_id` varchar(12) NOT NULL,
  `npc_name` varchar(30) CHARACTER SET utf8 NOT NULL,
  `posx` double NOT NULL,
  `posy` double NOT NULL,
  `posz` double NOT NULL,
  `rotation` float NOT NULL,
  `pitch` float NOT NULL,
  PRIMARY KEY (`npc_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

CREATE TABLE  `minecraft`.`quests` (
  `id` varchar(12) NOT NULL,
  `quest_name` text CHARACTER SET utf8 NOT NULL,
  `start_npc` varchar(12) NOT NULL,
  `end_npc` varchar(12) NOT NULL,
  `items_provided` text CHARACTER SET utf8,
  `rewards` text CHARACTER SET utf8,
  `quest_desc` mediumtext CHARACTER SET utf8 NOT NULL,
  `quest_type` varchar(10) NOT NULL,
  `prereq` varchar(12) DEFAULT NULL,
  `location` text,
  `data` text,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

CREATE TABLE  `minecraft`.`quests_active` (
  `player_name` varchar(25) NOT NULL,
  `quest_id` varchar(12) NOT NULL,
  `progress` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`player_name`,`quest_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

CREATE TABLE  `minecraft`.`quests_completed` (
  `player_name` varchar(25) NOT NULL,
  `quest_id` varchar(12) NOT NULL,
  `date_completed` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`player_name`,`quest_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- v0.2 update

ALTER TABLE `minecraft`.`craftizens` ADD COLUMN `item_in_hand` INT  NOT NULL DEFAULT 0 AFTER `pitch`;

-- v0.5 update

ALTER TABLE `minecraft`.`quests` ADD COLUMN `completion_text` TEXT  DEFAULT NULL AFTER `data`;

CREATE TABLE  `minecraft`.`craftizens_dialog` (
  `npc_id` varchar(12) NOT NULL,
  `dialog_id` varchar(12) NOT NULL,
  `dialog_text` text NOT NULL,
  PRIMARY KEY (`npc_id`,`dialog_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- v0.6 update

ALTER TABLE `minecraft`.`craftizens` ADD COLUMN `route_type` VARCHAR(8)  DEFAULT NULL AFTER `item_in_hand`,
 ADD COLUMN `route` TEXT  DEFAULT NULL AFTER `route_type`;

-- v0.7 update
ALTER TABLE `minecraft`.`quests` ADD COLUMN `rankreq` VARCHAR(12) DEFAULT NULL AFTER `prereq`,
 ADD COLUMN `rankreward` VARCHAR(12) DEFAULT NULL AFTER `rankreq`;

-- v0.7.1 update
ALTER TABLE `minecraft`.`quests` ADD COLUMN `cost` VARCHAR(12) DEFAULT '0' AFTER `rankreward`,
 ADD COLUMN `prize` VARCHAR(12) DEFAULT '0' AFTER `cost`;

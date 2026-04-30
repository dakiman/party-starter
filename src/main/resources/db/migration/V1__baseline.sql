
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `artist` (
  `spotify_id` varchar(255) NOT NULL,
  `images` json DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `spotify_url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`spotify_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `artist_genres` (
  `artist_id` varchar(255) NOT NULL,
  `genre` varchar(255) DEFAULT NULL,
  KEY `FKdjyqeqf2pox3e538n8avmdmc6` (`artist_id`),
  CONSTRAINT `FKdjyqeqf2pox3e538n8avmdmc6` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`spotify_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `drink` (
  `id` int NOT NULL,
  `external_id` int NOT NULL,
  `is_alcoholic` bit(1) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `recipe` varchar(10000) DEFAULT NULL,
  `thumbnail` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `drink_ingredient` (
  `id` bigint NOT NULL,
  `amount` varchar(255) DEFAULT NULL,
  `drink_id` int DEFAULT NULL,
  `ingredient_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1qrijlgpm74a4v5yw6slnrfbk` (`drink_id`),
  KEY `FK5t0467q5erfgck3sm3t5ro4h7` (`ingredient_id`),
  CONSTRAINT `FK1qrijlgpm74a4v5yw6slnrfbk` FOREIGN KEY (`drink_id`) REFERENCES `drink` (`id`),
  CONSTRAINT `FK5t0467q5erfgck3sm3t5ro4h7` FOREIGN KEY (`ingredient_id`) REFERENCES `ingredient` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `drink_ingredient_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `drink_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `event` (
  `id` int NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `date` date DEFAULT NULL,
  `is_private` bit(1) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `time` time(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `creator_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfnvc9jgli0tyfktl08q7h5rgd` (`creator_id`),
  CONSTRAINT `FKfnvc9jgli0tyfktl08q7h5rgd` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `event_artists` (
  `event_id` int NOT NULL,
  `artist_id` varchar(255) NOT NULL,
  PRIMARY KEY (`event_id`,`artist_id`),
  KEY `FKqft2n59i0o8awsgb4ss8i5pfm` (`artist_id`),
  CONSTRAINT `FK6lp5wkxbj09juq8fl39cu8khp` FOREIGN KEY (`event_id`) REFERENCES `event` (`id`),
  CONSTRAINT `FKqft2n59i0o8awsgb4ss8i5pfm` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`spotify_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `event_drinks` (
  `event_id` int NOT NULL,
  `drink_id` int NOT NULL,
  KEY `FKfk46ko6wp4b7xeft5lq6agdq` (`drink_id`),
  KEY `FKj9r2e6ij23o56d41c1esy6k23` (`event_id`),
  CONSTRAINT `FKfk46ko6wp4b7xeft5lq6agdq` FOREIGN KEY (`drink_id`) REFERENCES `drink` (`id`),
  CONSTRAINT `FKj9r2e6ij23o56d41c1esy6k23` FOREIGN KEY (`event_id`) REFERENCES `event` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `event_food_items` (
  `event_id` int NOT NULL,
  `food_item` varchar(255) DEFAULT NULL,
  KEY `FKmlbqiml6d3b2mww85jo7pybxy` (`event_id`),
  CONSTRAINT `FKmlbqiml6d3b2mww85jo7pybxy` FOREIGN KEY (`event_id`) REFERENCES `event` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `event_ingredients` (
  `event_id` int NOT NULL,
  `ingredient_id` int NOT NULL,
  KEY `FK83otggh9l0ym66vgqfi2samqu` (`ingredient_id`),
  KEY `FKfbq3hbpk27a8gdfgmf65vmx7v` (`event_id`),
  CONSTRAINT `FK83otggh9l0ym66vgqfi2samqu` FOREIGN KEY (`ingredient_id`) REFERENCES `ingredient` (`id`),
  CONSTRAINT `FKfbq3hbpk27a8gdfgmf65vmx7v` FOREIGN KEY (`event_id`) REFERENCES `event` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `event_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ingredient` (
  `id` int NOT NULL,
  `abv` varchar(255) DEFAULT NULL,
  `description` varchar(10000) DEFAULT NULL,
  `is_alcoholic` bit(1) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ingredient_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_ob8kqyqqgmefl0aco34akdtpe` (`email`),
  UNIQUE KEY `UK_sb8bbouer5wak8vyiiy4pf2bx` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;


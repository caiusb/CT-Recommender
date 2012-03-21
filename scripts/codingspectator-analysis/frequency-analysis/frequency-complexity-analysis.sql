--This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

\p Computing the total number of performed refactorings of each complexity level

DROP TABLE "PUBLIC"."REFACTORING_COMPLEXITY_COUNTS" IF EXISTS;

CREATE TABLE "PUBLIC"."REFACTORING_COMPLEXITY_COUNTS" (

  "COMPLEXITY" VARCHAR(5),

  "CODINGTRACKER_PERFORMED_COUNT" INT

);

INSERT INTO "PUBLIC"."REFACTORING_COMPLEXITY_COUNTS" (

  "COMPLEXITY",

  "CODINGTRACKER_PERFORMED_COUNT"

)

SELECT 

(SELECT "RC"."COMPLEXITY"

FROM "PUBLIC"."REFACTORING_COMPLEXITY" "RC"

WHERE "RC"."REFACTORING_ID" = "R"."REFACTORING_ID") AS "COMPLEXITY",

"R"."CODINGTRACKER_PERFORMED_COUNT" AS "CODINGTRACKER_PERFORMED_COUNT"

FROM "PUBLIC"."PER_REFACTORING_ID" "R"

WHERE IS_JAVA_REFACTORING("R"."REFACTORING_ID");

DROP TABLE "PUBLIC"."PER_COMPLEXITY" IF EXISTS;

CREATE TABLE "PUBLIC"."PER_COMPLEXITY" (

  "COMPLEXITY" VARCHAR(5),

  "CODINGTRACKER_PERFORMED_COUNT" INT

);

INSERT INTO "PUBLIC"."PER_COMPLEXITY" (

  "COMPLEXITY",

  "CODINGTRACKER_PERFORMED_COUNT"

)

SELECT 

"T"."COMPLEXITY" AS "COMPLEXITY",

SUM("T"."CODINGTRACKER_PERFORMED_COUNT") AS "CODINGTRACKER_PERFORMED_COUNT"

FROM "PUBLIC"."REFACTORING_COMPLEXITY_COUNTS" "T"

WHERE "T"."COMPLEXITY" IN ('S', 'M', 'C')

GROUP BY "T"."COMPLEXITY"

ORDER BY "T"."COMPLEXITY" DESC;

* *DSV_COL_DELIM =,

* *DSV_ROW_DELIM =\n

* *DSV_TARGET_FILE =PerComplexity.csv

\x SELECT * FROM "PUBLIC"."PER_COMPLEXITY"

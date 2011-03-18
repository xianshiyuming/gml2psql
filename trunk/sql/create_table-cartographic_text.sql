-- Table: cartographic_text

DROP TABLE cartographic_text;

CREATE TABLE cartographic_text
(
  fid character(20) NOT NULL,
  feature_code integer,
  "version" integer,
  version_date character(10),
  theme character(25),
  descriptive_group character(27),
  physical_level integer,
  text_string character(35),
  make character(12),
  descriptive_term character(35),
  anchor_position integer,
  font integer,
  height decimal,
  orientation integer,
  CONSTRAINT cartographic_text_pk PRIMARY KEY (fid)
)
WITH (OIDS=FALSE);
ALTER TABLE cartographic_text OWNER TO postgres;

--AddGeometryColumn(<schema_name>, <table_name>, <column_name>, <srid>, <type>, <dimension>)

SELECT AddGeometryColumn('public', 'cartographic_text', 'geom', 27700, 'POINT', 2);

-- Index: cartographictext_geom_idx

-- DROP INDEX cartographictext_geom_idx;

CREATE INDEX cartographictext_geom_idx
  ON cartographic_text
  USING gist
  (geom);
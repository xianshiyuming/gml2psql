-- Table: cartographic_text

DROP TABLE topographic_area;

CREATE TABLE topographic_area
(
  fid character(20) NOT NULL,
  feature_code integer,
  "version" integer,
  version_date character(10),
  theme character(25),
  descriptive_group character(35),
  descriptive_term character(35),
  physical_level integer,
  make character(12),
  calculated_area decimal,
  CONSTRAINT topographic_area_pk PRIMARY KEY (fid)
)
WITH (OIDS=FALSE);
ALTER TABLE topographic_area OWNER TO postgres;

SELECT AddGeometryColumn('public', 'topographic_area', 'geom', 27700, 'POLYGON', 2);

-- Index: cartographictext_geom_idx

-- DROP INDEX cartographictext_geom_idx;

CREATE INDEX topographic_area_geom_idx
  ON topographic_area
  USING gist
  (geom);
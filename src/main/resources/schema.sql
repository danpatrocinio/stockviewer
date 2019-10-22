DROP TABLE IF EXISTS ativo;

CREATE TABLE IF NOT EXISTS ativo (
  id_ativo  INT NOT NULL PRIMARY KEY,
  ticker    VARCHAR(7) NOT NULL,
  nome      VARCHAR(200) NOT NULL,
  cotacao   DECIMAL(10,2)
);

DROP TABLE IF EXISTS operacao;

CREATE TABLE IF NOT EXISTS operacao (
  id_operacao       INT NOT NULL PRIMARY KEY,
  carimbo_data_hora TIMESTAMP (9) NOT NULL,
  tipo              VARCHAR(6) NOT NULL,
  data              DATE,
  id_ativo          INT,
  quantidade        INT,
  valor_unitario    DECIMAL(10,2),
  corretora         VARCHAR(30)
);

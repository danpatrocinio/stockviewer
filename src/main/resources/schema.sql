DROP TABLE IF EXISTS ativo;

CREATE TABLE IF NOT EXISTS ativo (
  id_ativo  INT NOT NULL PRIMARY KEY,
  ticker    VARCHAR(7) NOT NULL,
  nome      VARCHAR(200) NOT NULL,
  cotacao   DECIMAL(12,4),
  classe_ativo VARCHAR(1) NOT NULL
);

DROP TABLE IF EXISTS operacao;

CREATE TABLE IF NOT EXISTS operacao (
  id_operacao       INT NOT NULL PRIMARY KEY,
  carimbo_data_hora TIMESTAMP (9) NOT NULL,
  tipo              VARCHAR(6) NOT NULL,
  data              DATE,
  id_ativo          INT,
  quantidade        DECIMAL(15,10),
  valor_unitario    DECIMAL(12,4),
  corretora         VARCHAR(30)
);

DROP TABLE IF EXISTS carteira;

CREATE TABLE IF NOT EXISTS carteira (
  id_carteira       INT NOT NULL PRIMARY KEY,
  valor_custo       DECIMAL(12,4),
  valor_mercado     DECIMAL(12,4)
);

DROP TABLE IF EXISTS ativo_carteira;

CREATE TABLE IF NOT EXISTS ativo_carteira (
  id_ativo_carteira INT NOT NULL PRIMARY KEY,
  id_ativo          INT,
  quantidade        DECIMAL(15,10),
  preco_medio       DECIMAL(12,4)
);
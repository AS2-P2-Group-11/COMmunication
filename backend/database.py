from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from .common import ConfigManager

config_manager = ConfigManager()
db = {
    "host": config_manager.get("DATABASE", "host"),
    "port": config_manager.get("DATABASE", "port"),
    "username": config_manager.get("DATABASE", "username"),
    "password": config_manager.get("DATABASE", "password"),
    "database": config_manager.get("DATABASE", "database")
}
SQLALCHEMY_DATABASE_URL = "mysql+pymysql://"+db['username']+":"+db['password']+ \
                        "@"+db['host']+ ":" +db['port']+"/"+db['database']
engine = create_engine(
    SQLALCHEMY_DATABASE_URL
)

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()
import os
try:
    from configparser import ConfigParser, NoOptionError, NoSectionError, MissingSectionHeaderError, ParsingError
except ImportError:
    from ConfigParser import ConfigParser, NoOptionError, NoSectionError, MissingSectionHeaderError, ParsingError


class ConfigManager(ConfigParser):
    """Configuration Manager"""

    def __init__(self):
        """Initialize """
        super(ConfigManager, self).__init__()
        self._configfile = "../config.ini"
        self._read_config()

    def _read_config(self):
        """Read Configuration file"""
        if os.path.exists(self._configfile):
            try:
                ConfigManager.read(self, self._configfile)
            except MissingSectionHeaderError:
                return ("Error Missing Section header"
                        "in config file: {}").format(self._configfile)
            except ParsingError:
                return ("Error in parsing"
                        "config file: {}").format(self._configfile)


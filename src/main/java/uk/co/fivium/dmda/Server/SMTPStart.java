package uk.co.fivium.dmda.Server;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import uk.co.fivium.dmda.AntiVirus.AVScannerFactory;
import uk.co.fivium.dmda.DatabaseConnection.DatabaseConnectionException;
import uk.co.fivium.dmda.DatabaseConnection.DatabaseConnectionHandler;

import java.io.IOException;

public class SMTPStart {
  public static void main(String[] args) {
    final SMTPStart lSMTPStart = new SMTPStart();

    Thread lShutdownHook = new Thread(){
      @Override
      public void run() {
        lSMTPStart.stop();
      }
    };
    lShutdownHook.setName("Shutdown Hook");
    Runtime.getRuntime().addShutdownHook(lShutdownHook);

    lSMTPStart.start();
  }

  public void start(){
    BasicConfigurator.configure(); // Enable some sort of basic logging
    try {
      loadServerConfiguration();
      startSMTPServer();
    }
    catch (Exception ex){
      Logger.getRootLogger().error("Error during startup. Server shutting down.", ex);
    }
  }

  public void stop() {
    DatabaseConnectionHandler.getInstance().shutDown();
    Logger.getRootLogger().info("Shutdown signal received. Server shutting down.");
  }

  private void startSMTPServer()
  throws ServerStartupException {
    try {
      DatabaseConnectionHandler.getInstance().createConnectionPools();
    }
    catch (DatabaseConnectionException ex) {
      throw new ServerStartupException("Failed to create database connection pools", ex);
    }

    try{
      AVScannerFactory.getScanner().testConnection();
    }
    catch (IOException ex) {
      throw new ServerStartupException("Failed to connect to anti-virus scanner", ex);
    }

    SMTPServerWrapper lServer = new SMTPServerWrapper();
    lServer.start();
  }

  private void loadServerConfiguration()
  throws ServerStartupException {
    try {
      SMTPConfig.getInstance().loadConfig();
    }
    catch (ConfigurationException ex) {
      throw new ServerStartupException("Failed to load server configuration", ex);
    }
  }
}

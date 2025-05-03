package gui;

import java.io.Serializable;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Represents a connected client on the server.
 * This class encapsulates the client's information, including their unique ID, IP address, 
 * and connection status, using JavaFX properties to enable data binding.
 * 
 * <p>The class implements {@link Serializable} to allow client information to be 
 * serialized for network or file operations.</p>
 */
public class ClientInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    	/**
    	 * The unique identifier for the client.
    	 * Stored as a {@link SimpleLongProperty} to support JavaFX property bindings.
    	 */
        private final SimpleLongProperty clientId;   
        /**
         * The IP address of the client.
         * Stored as a {@link SimpleStringProperty} to support JavaFX property bindings.
         */
        private final SimpleStringProperty ipAddress;
        /**
         * The connection status of the client.
         * Stored as a {@link SimpleStringProperty} to support JavaFX property bindings.
         * The default value is "Connected".
         */
        private final SimpleStringProperty status;   
        
        /**
         * Constructs a new {@code ClientInfo} instance with the specified client ID 
         * and IP address. The default connection status is "Connected".
         *
         * @param clientId  The unique identifier of the client.
         * @param ipAddress The IP address of the client.
         */
        public ClientInfo(long clientId, String ipAddress) {
            this.clientId = new SimpleLongProperty(clientId);
            this.ipAddress = new SimpleStringProperty(ipAddress);
            this.status = new SimpleStringProperty("Connected"); // Default status
        }
        /**
         * Gets the client's unique identifier.
         *
         * @return The client's unique identifier.
         */
        public long getClientId() {
            return clientId.get();
        }
        /**
         * Sets the client's unique identifier.
         *
         * @param clientId The new unique identifier for the client.
         */
        public void setClientId(long clientId) {
            this.clientId.set(clientId);
        }
        /**
         * Gets the client's IP address.
         *
         * @return The client's IP address.
         */
        public String getIpAddress() {
            return ipAddress.get();
        }
        /**
         * Sets the client's IP address.
         *
         * @param ipAddress The new IP address for the client.
         */
        public void setIpAddress(String ipAddress) {
            this.ipAddress.set(ipAddress);
        }
        /**
         * Gets the client's connection status.
         *
         * @return The client's connection status.
         */
        public String getStatus() {
            return status.get();
        }
        /**
         * Sets the client's connection status.
         *
         * @param status The new connection status for the client.
         */
        public void setStatus(String status) {
            this.status.set(status);
        }
        /**
         * Gets the {@link SimpleLongProperty} for the client's ID, allowing for JavaFX property binding.
         *
         * @return The {@link SimpleLongProperty} representing the client's ID.
         */
        public SimpleLongProperty clientIdProperty() {
            return clientId;
        }
        /**
         * Gets the {@link SimpleStringProperty} for the client's IP address, allowing for JavaFX property binding.
         *
         * @return The {@link SimpleStringProperty} representing the client's IP address.
         */
        public SimpleStringProperty ipAddressProperty() {
            return ipAddress;
        }
        /**
         * Gets the {@link SimpleStringProperty} for the client's connection status, allowing for JavaFX property binding.
         *
         * @return The {@link SimpleStringProperty} representing the client's connection status.
         */
        public SimpleStringProperty statusProperty() {
            return status;
        }
    }



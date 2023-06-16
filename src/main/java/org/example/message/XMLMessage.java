package org.example.message;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.StringWriter;
import java.util.UUID;

public class XMLMessage {
    private UUID id;
    private final String sender;
    private final MessageType messageType;
    private String text;

    public XMLMessage(String sender, MessageType messageType, String text) {
        this.id = UUID.randomUUID();
        this.sender = sender;
        this.messageType = messageType;
        this.text = text;
    }

    public String getSender() {
        return sender;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String toXMLString() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            // Create the root element
            Element messageElement = document.createElement("Message");
            document.appendChild(messageElement);

            // Create and append child elements
            Element idElement = document.createElement("Id");
            idElement.setTextContent(id.toString());
            messageElement.appendChild(idElement);

            Element senderElement = document.createElement("Sender");
            senderElement.setTextContent(sender);
            messageElement.appendChild(senderElement);

            Element typeElement = document.createElement("Type");
            typeElement.setTextContent(messageType.toString());
            messageElement.appendChild(typeElement);

            Element textElement = document.createElement("Text");
            textElement.setTextContent(text);
            messageElement.appendChild(textElement);

            // Convert the document to a string
            StringWriter writer = new StringWriter();
            javax.xml.transform.Transformer transformer = javax.xml.transform.TransformerFactory.newInstance().newTransformer();
            transformer.transform(new javax.xml.transform.dom.DOMSource(document), new javax.xml.transform.stream.StreamResult(writer));

            return writer.toString();
        } catch (ParserConfigurationException | javax.xml.transform.TransformerException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static XMLMessage fromXMLString(String xml) {
        try {
            System.out.println(xml);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new org.xml.sax.InputSource(new java.io.StringReader(xml)));

            Element messageElement = document.getDocumentElement();

            // Retrieve values from XML elements
            String idString = getElementTextContent(messageElement, "Id");
            assert idString != null;
            UUID id = UUID.fromString(idString);

            String sender = getElementTextContent(messageElement, "Sender");

            String typeString = getElementTextContent(messageElement, "Type");
            MessageType type = MessageType.valueOf(typeString);

            String text = getElementTextContent(messageElement, "Text");

            XMLMessage message = new XMLMessage(sender, type, text);
            message.id = id; // Set the ID separately

            return message;
        } catch (org.xml.sax.SAXException | java.io.IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getElementTextContent(Element element, String tagName) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            Element tagElement = (Element) nodeList.item(0);
            return tagElement.getTextContent();
        }
        return null;
    }

    public MessageType getMessageType() {
        return this.messageType;
    }
}
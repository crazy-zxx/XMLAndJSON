package com.me.test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.*;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class Test {

    public static void main(String[] args) throws Exception {

        /**
         * XML是可扩展标记语言（eXtensible Markup Language）的缩写，它是是一种数据表示格式，
         * 可以描述非常复杂的数据结构，常用于传输和存储数据。
         *
         * XML有几个特点：一是纯文本，默认使用UTF-8编码，二是可嵌套，适合表示结构化数据。
         *
         * XML有固定的结构，首行必定是<?xml version="1.0"?>，可以加上可选的编码。
         * 紧接着，如果以类似<!DOCTYPE note SYSTEM "book.dtd">声明的是文档定义类型（DTD：Document Type Definition），DTD是可选的。
         * 接下来是XML的文档内容，一个XML文档有且仅有一个根元素，根元素可以包含任意个子元素，元素可以包含属性，
         * 例如，<isbn lang="CN">1234567</isbn>包含一个属性lang="CN"，且元素必须正确嵌套。
         * 如果是空元素，可以用<tag/>表示。
         *
         * 由于使用了<、>以及引号等标识符，如果内容出现了特殊符号，需要使用 &???; 表示转义。
         * 常见的特殊字符如下：
         * 字符	表示
         * <	&lt;
         * >	&gt;
         * &	&amp;
         * "	&quot;
         * '	&apos;
         *
         * 格式正确的XML（Well Formed）是指XML的格式是正确的，可以被解析器正常读取。
         * 而合法的XML是指，不但XML格式正确，而且它的数据结构可以被DTD或者XSD验证。
         *
         * 如何验证XML文件的正确性呢？最简单的方式是通过浏览器验证。可以直接把XML文件拖拽到浏览器窗口，如果格式错误，浏览器会报错。
         *
         * 和结构类似的HTML不同，浏览器对HTML有一定的“容错性”，缺少关闭标签也可以被解析，但XML要求严格的格式，任何没有正确嵌套的标签都会导致错误。
         */

        /**
         * XML是一种树形结构的文档，它有两种标准的解析API：
         *     DOM：一次性读取XML，并在内存中表示为树形结构；
         *     SAX：以流的形式读取XML，使用事件回调。
         */

         /** DOM(Document Object Model):
         * 最顶层的document代表XML文档，它是真正的“根”
         * Java提供了DOM API来解析XML，它使用下面的对象来表示XML的内容：
         *     Document：代表整个XML文档；
         *     Element：代表一个XML元素；
         *     Attribute：代表一个元素的某个属性。
         * 使用DOM API时，如果要读取某个元素的文本，需要访问它的Text类型的子节点，所以使用起来还是比较繁琐的。
         */
        InputStream input1 = Test.class.getResourceAsStream("/book.xml");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        //DocumentBuilder.parse()用于解析一个XML，它可以接收InputStream，File或者URL，
        //如果解析无误，我们将获得一个Document对象，这个对象代表了整个XML文档的树形结构，需要遍历以便读取指定元素的值：
        Document doc = db.parse(input1);
        input1.close();
        printNode(doc,0);


        /**
         * SAX(Simple API for XML):
         * 它是一种基于流的解析方式，边读取XML边解析，并以事件回调的方式让调用者获取数据。
         * 因为是一边读一边解析，所以无论XML有多大，占用的内存都很小。
         *
         * SAX解析会触发一系列事件：
         *     startDocument：开始读取XML文档；
         *     startElement：读取到了一个元素，例如<book>；
         *     characters：读取到了字符；
         *     endElement：读取到了一个结束的元素，例如</book>；
         *     endDocument：读取XML文档结束。
         *
         * 如果要读取<name>节点的文本，我们就必须在解析过程中根据startElement()和endElement()定位当前正在读取的节点，
         * 可以使用栈结构保存，每遇到一个startElement()入栈，每遇到一个endElement()出栈，
         * 这样，读到characters()时我们才知道当前读取的文本是哪个节点的。可见，使用SAX API仍然比较麻烦。
         */
        InputStream input2 = Test.class.getResourceAsStream("/book.xml");
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser saxParser = spf.newSAXParser();
        saxParser.parse(input2, new MyHandler());
        input2.close();


        /**
         * 一个名叫Jackson的开源的第三方库可以轻松做到XML到JavaBean的转换,需要添加相关的maven配置
         * 使用Jackson解析XML，可以直接把XML解析为JavaBean，十分方便。
         */
        InputStream input3 = Test.class.getResourceAsStream("/book.xml");
        JacksonXmlModule module = new JacksonXmlModule();
        XmlMapper mapper3 = new XmlMapper(module);
        Book book3 = mapper3.readValue(input3, Book.class);
        System.out.println(book3);


        /**
         * JSON是JavaScript Object Notation的缩写，它去除了所有JavaScript执行代码，只保留JavaScript的对象格式。
         *
         * JSON作为数据传输的格式，有几个显著的优点：
         *     JSON只允许使用UTF-8编码，不存在编码问题；
         *     JSON只允许使用双引号作为key，特殊字符用\转义，格式简单；
         *     浏览器内置JSON支持，如果把数据用JSON发送给浏览器，可以用JavaScript直接处理。
         *
         * JSON适合表示层次结构，因为它格式简单，仅支持以下几种数据类型：
         *     键值对：{"key": value}
         *     数组：[1, 2, 3]
         *     字符串："abc"
         *     数值（整数和浮点数）：12.34
         *     布尔值：true或false
         *     空值：null
         *
         * 常用的用于解析JSON的第三方库有：
         *     Jackson
         *     Gson
         *     Fastjson
         *     ...
         *
         * 把JSON解析为JavaBean的过程称为反序列化。
         * 如果把JavaBean变为JSON，那就是序列化。
         *
         *
         */
        //用Jackson解析json
        //反序列化
        InputStream input4 = Test.class.getResourceAsStream("/book.json");
        //要把JSON的某些值解析为特定的Java对象，例如LocalDate，也是完全可以的
        //在创建ObjectMapper时，注册一个新的JavaTimeModule
        ObjectMapper mapper4 = new ObjectMapper().registerModule(new JavaTimeModule());
        //反序列化时忽略不存在的JavaBean属性:
        //关闭DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES功能使得解析时如果JavaBean不存在该属性时解析不会报错。
        mapper4.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Book book4 = mapper4.readValue(input4, Book.class);
        System.out.println(book4);

        //序列化
        String json = mapper4.writeValueAsString(book4);
        System.out.println(json);

        //内置的解析规则和扩展的解析规则如果都不满足我们的需求，还可以自定义解析。
        //需要自定义一个xxxDeserializer extends JsonDeserializer<T>，用于解析含有非数字的字符串
        //还要在JavaBeen的相应字段上做注解


    }

    static void printNode(Node n, int indent) {
        for (int i = 0; i < indent; i++) {
            System.out.print(' ');
        }
        switch (n.getNodeType()) {
            case Node.DOCUMENT_NODE: // Document节点
                System.out.println("Document: " + n.getNodeName());
                break;
            case Node.ELEMENT_NODE: // 元素节点
                System.out.println("Element: " + n.getNodeName());
                break;
            case Node.TEXT_NODE: // 文本
                System.out.println("Text: " + n.getNodeName() + " = " + n.getNodeValue());
                break;
            case Node.ATTRIBUTE_NODE: // 属性
                System.out.println("Attr: " + n.getNodeName() + " = " + n.getNodeValue());
                break;
            default: // 其他
                System.out.println("NodeType: " + n.getNodeType() + ", NodeName: " + n.getNodeName());
        }
        for (Node child = n.getFirstChild(); child != null; child = child.getNextSibling()) {
            printNode(child, indent + 1);
        }
    }

}

//需要自定义一个PriceDeserializer，用于解析含有非数字的字符串
class PriceDeserializer extends JsonDeserializer<BigDecimal> {
    public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        // 读取原始的JSON字符串内容:
        String s = p.getValueAsString();
        if (s != null) {
            try {
                return new BigDecimal(s.replace(",", ""));
            } catch (NumberFormatException e) {
                throw new JsonParseException(p, s, e);
            }
        }
        return null;
    }
}


class MyHandler extends DefaultHandler {
    public void startDocument() throws SAXException {
        print("start document");
    }

    public void endDocument() throws SAXException {
        print("end document");
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        print("start element:", localName, qName);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        print("end element:", localName, qName);
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        print("characters:", new String(ch, start, length));
    }

    public void error(SAXParseException e) throws SAXException {
        print("error:", e);
    }

    void print(Object... objs) {
        for (Object obj : objs) {
            System.out.print(obj);
            System.out.print(" ");
        }
        System.out.println();
    }
}

class Book {
    public long id;
    public String name;
    public String author;
    public String isbn;
    public List<String> tags;
    public LocalDate pubDate;
    // 表示反序列化price时使用自定义的PriceDeserializer
    @JsonDeserialize(using = PriceDeserializer.class)
    public BigDecimal price;

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", isbn='" + isbn + '\'' +
                ", tags=" + tags +
                ", pubDate=" + pubDate +
                ", price=" + price +
                '}';
    }
}
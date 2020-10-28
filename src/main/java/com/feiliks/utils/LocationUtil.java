package com.feiliks.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.*;

public final class LocationUtil {

    private static LocationUtil instance = null;
    private final Trie<LocationEntity> data = new Trie<LocationEntity>();

    LocationUtil() {
        DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = bf.newDocumentBuilder();
            Document doc = db.parse(LocationUtil.class.getResourceAsStream("/Locations.xml"));
            loadData(doc);
        } catch (Exception e) {
        }
    }

    public static LocationUtil getInstance() {
        if (instance == null) {
            instance = new LocationUtil();
        }
        return instance;
    }

    public static void reload() {
        instance = new LocationUtil();
    }

    public static Address parse(String text) {
        return getInstance().parseAddress(text);
    }

    public static void main(String[] args) {
        System.out.println(parse("江苏苏州市"));
        System.out.println();
        System.out.println(parse("江苏省无锡"));
        System.out.println();
        System.out.println(parse("重庆市渝北区龙兴镇迎龙大道"));
        System.out.println();
        System.out.println(parse("重庆市渝北区双凤桥空港大道579号"));
        System.out.println();
        System.out.println(parse("深圳宝安区沙井镇南环路1号"));
        System.out.println();
        System.out.println(parse("东莞市长安镇乌沙江贝第三工业区步步高大道124号"));
        System.out.println();
        System.out.println(parse("广东省东莞市黄江镇裕园工业区"));
        System.out.println();
        System.out.println(parse("合肥"));
        System.out.println();
        System.out.println(parse("广州保税区保盈南路19号"));
        System.out.println();
        System.out.println(parse("上海市嘉定区嘉松北路 蕴藻浜大桥西首"));
        System.out.println();
        System.out.println(parse("江苏省苏州市新区滨河路813号溢智园5号楼2楼"));
        System.out.println();
        System.out.println(parse("苏州相城区黄岱镇潘杨工业园"));
        System.out.println();
        System.out.println(parse("台北市内湖区民权东路6段11巷43-1号4楼"));
        System.out.println();
        System.out.println(parse("台湾桃园县中坜市东园路13号"));
        System.out.println();
        System.out.println(parse("香港新界粉岭安乐村业丰街20号利亨中心3楼18室"));
        System.out.println();
        System.out.println(parse("江苏省南京市栖霞区迈皋桥创业园 7号"));
        System.out.println();
        System.out.println(parse("河南省郑州市中牟县白沙镇商都路4401号"));
        System.out.println();
        System.out.println(parse("江蘇蘇州市"));
        System.out.println();
        System.out.println(parse("江蘇省無錫"));
        System.out.println();
        System.out.println(parse("重慶市渝北區龍興鎮迎龍大道"));
        System.out.println();
        System.out.println(parse("重慶市渝北區雙鳳橋空港大道579號"));
        System.out.println();
        System.out.println(parse("深圳寶安區沙井鎮南環路1號"));
        System.out.println();
        System.out.println(parse("東莞市長安鎮烏沙江貝第三工業區步步高大道124號"));
        System.out.println();
        System.out.println(parse("廣東省東莞市黃江鎮裕園工業區"));

    }

    private Element getAliasNode(Element node) {
        NodeList children = node.getChildNodes();
        int l = children.getLength();
        for (int i = 0; i < l; i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("Alias")) {
                return (Element) n;
            }
        }
        return null;
    }

    private Set<String> getAlias(Element node) {
        Set<String> out = new HashSet<String>();
        Element aliasNode = getAliasNode(node);
        if (aliasNode != null) {
            NodeList list = aliasNode.getElementsByTagName("Name");
            int l = list.getLength();
            for (int i = 0; i < l; ++i) {
                out.add(list.item(i).getTextContent().trim());
            }
        }
        return out;
    }

    private void loadData(Document doc) {
        NodeList countries = doc.getElementsByTagName("CountryRegion");
        for (int i = 0; i < countries.getLength(); ++i) {
            Element c = (Element) countries.item(i);
            LocationEntity entity = new LocationEntity(
                    LocationEntityType.COUNTRY,
                    c.getAttribute("Name"));
            data.index(entity.name, entity);
            for (String name : getAlias(c)) {
                data.index(name, entity);
            }
            loadStates(entity, c);
        }
    }

    private void loadStates(LocationEntity parent, Node countryNode) {
        Element e = (Element) countryNode;
        NodeList states = e.getElementsByTagName("State");
        for (int i = 0; i < states.getLength(); ++i) {
            Element s = (Element) states.item(i);
            LocationEntity entity = new LocationEntity(
                    LocationEntityType.STATE,
                    s.getAttribute("Name"),
                    parent);
            data.index(entity.name, entity);
            for (String name : getAlias(s)) {
                data.index(name, entity);
            }
            loadCities(entity, s);
        }
    }

    private void loadCities(LocationEntity parent, Node stateNode) {
        Element e = (Element) stateNode;
        NodeList cities = e.getElementsByTagName("City");
        for (int i = 0; i < cities.getLength(); ++i) {
            Element c = (Element) cities.item(i);
            LocationEntity entity = new LocationEntity(
                    LocationEntityType.CITY,
                    c.getAttribute("Name"),
                    parent);
            data.index(entity.name, entity);
            for (String name : getAlias(c)) {
                data.index(name, entity);
            }
            loadRegions(entity, c);
        }
    }

    private void loadRegions(LocationEntity parent, Node cityNode) {
        Element e = (Element) cityNode;
        NodeList regions = e.getElementsByTagName("Region");
        for (int i = 0; i < regions.getLength(); ++i) {
            Element r = (Element) regions.item(i);
            LocationEntity entity = new LocationEntity(
                    LocationEntityType.REGION,
                    r.getAttribute("Name"),
                    parent);
            String t;
            t = r.getAttribute("Areacode");
            entity.areacode = t == null || t.trim().isEmpty() ? null : t.trim();
            t = r.getAttribute("Postcode");
            entity.postcode = t == null || t.trim().isEmpty() ? null : t.trim();
            data.index(entity.name, entity);
            for (String name : getAlias(r)) {
                data.index(name, entity);
            }
        }
    }

    private final static class RatedAddressWrapper implements Comparable<RatedAddressWrapper> {

        float rating = 0.0F;
        Address address = new Address();

        RatedAddressWrapper(LocationEntity entity) {
            setLocationEntity(entity);
        }

        void setLocationEntity(LocationEntity entity) {
            switch (entity.type) {
                case COUNTRY:
                    address.country = entity.name;
                    break;
                case STATE:
                    address.state = entity.name;
                    break;
                case CITY:
                    address.city = entity.name;
                    break;
                case REGION:
                    address.region = entity.name;
                    address.postcode = entity.postcode;
                    address.areacode = entity.areacode;
                    break;
            }
        }

        @Override
        public int compareTo(RatedAddressWrapper o) {
            if (rating == o.rating) {
                return 0;
            }
            return rating - o.rating > 0 ? -1 : 1;
        }

        @Override
        public String toString() {
            return String.format("(rating: %f, address: %s)", rating, address.toString());
        }
    }

    private RatedAddressWrapper rateEntities(Set<LocationEntity> found, LocationEntity entity) {
        RatedAddressWrapper addr = new RatedAddressWrapper(entity);
        LocationEntity p = entity.parent;
        int count = 1;
        while (p != null) {
            addr.setLocationEntity(p);
            if (found.contains(p)) {
                count++;
            }
            p = p.parent;
        }
        addr.rating = 1.0F * count / found.size();
        return addr;
    }

    private Address parseAddress(String text) {
        PriorityQueue<RatedAddressWrapper> q = new PriorityQueue<RatedAddressWrapper>();
        Set<LocationEntity> entities = data.extract(text.replaceAll("[　\\s]", ""));
        for (LocationEntity entity : entities) {
            q.add(rateEntities(entities, entity));
        }
        //System.err.println("PriorityQueue");
        //System.err.println(q);
        RatedAddressWrapper out = q.peek();
        if (out == null) {
            return null;
        }
        return out.address;
    }

    interface TrieQueriable<T> {

        TrieNode<T> query(char c);
    }

    private enum TrieQueryStatus {
        S_INITIAL, S_HAS_MATCHES, S_EXACT_MATCH, S_NO_MATCH, S_END_OF_KEY
    }

    final static class TrieQuery<T> {

        private TrieQueryStatus status;
        private TrieQueriable<T> node;
        private TrieQueriable<T> start;

        TrieQuery(TrieQueriable<T> node) {
            this.start = node;
            this.node = node;
            this.status = TrieQueryStatus.S_INITIAL;
        }

        public TrieQueryStatus getStatus() {
            return status;
        }

        public TrieNode<T> getCurrentNode() {
            if (node == null || !(node instanceof TrieNode)) {
                return null;
            }
            return (TrieNode<T>) node;
        }

        public Set<T> getCurrentValues() {
            if (node == null || !(node instanceof TrieNode)) {
                return Collections.emptySet();
            }
            // return new HashSet<T>(((TrieNode<T>) node).values);
            return ((TrieNode<T>) node).values;
        }

        public void restart() {
            node = start;
        }

        public TrieQuery<T> query(char c) {
            if (c < 1) {
                status = TrieQueryStatus.S_END_OF_KEY;
                return this;
            }
            if (node == null) {
                status = TrieQueryStatus.S_NO_MATCH;
                return this;
            }
            node = node.query(c);
            if (node == null) {
                status = TrieQueryStatus.S_NO_MATCH;
                return this;
            }
            TrieNode<T> n = (TrieNode<T>) node;
            if (n.values.isEmpty()) {
                status = TrieQueryStatus.S_HAS_MATCHES;
                return this;
            }
            status = TrieQueryStatus.S_EXACT_MATCH;
            return this;
        }

    }

    final static class TrieNode<T> implements TrieQueriable<T> {

        private char current;
        private Map<Character, TrieNode<T>> next;
        private Set<T> values;

        TrieNode(char c, String text, T obj) {
            current = c;
            next = new HashMap<Character, TrieNode<T>>();
            values = new HashSet<T>();
            appendIndex(text, obj);
        }

        void appendIndex(String text, T obj) {
            if (text == null || text.isEmpty()) {
                values.add(obj);
            } else {
                char c = text.charAt(0);
                TrieNode<T> n = next.get(c);
                if (n == null) {
                    next.put(c, new TrieNode<T>(c, text.substring(1), obj));
                } else {
                    n.appendIndex(text.substring(1), obj);
                }
            }
        }

        /**
         * get an exact match of the supplied partial key from the current node.
         *
         * @param key String
         * @return Set<T>
         */
        public Set<T> get(String key) {
            if (key == null || key.isEmpty()) {
                return Collections.emptySet();
            }
            char c = key.charAt(0);
            if (c != current) {
                return Collections.emptySet();
            }
            int l = key.length();
            if (l == 1) {
                return new HashSet<T>(values);
            }
            TrieNode<T> n = next.get(key.charAt(1));
            if (n == null) {
                return Collections.emptySet();
            }
            return n.get(key.substring(1));
        }

        @Override
        public TrieNode<T> query(char c) {
            return next.get(c);
        }

    }

    final static class Trie<T> implements TrieQueriable<T> {

        private final Map<Character, TrieNode<T>> data = new HashMap<Character, TrieNode<T>>();

        /**
         * index object to key.
         *
         * @param key String
         * @param obj T
         */
        public void index(String key, T obj) {
            if (key == null || key.isEmpty()) {
                throw new IllegalArgumentException("key length must not be 0");
            }
            if (obj == null) {
                throw new IllegalArgumentException("no object to be indexed");
            }
            char c = key.charAt(0);
            TrieNode<T> n = data.get(c);
            if (n == null) {
                data.put(c, new TrieNode<T>(c, key.substring(1), obj));
            } else {
                n.appendIndex(key.substring(1), obj);
            }
        }

        /**
         * get an exact match of the supplied key.
         *
         * @param key String
         * @return Set<T>
         */
        public Set<T> get(String key) {
            if (key == null || key.isEmpty()) {
                return null;
            }
            char c = key.charAt(0);
            TrieNode<T> n = data.get(c);
            if (n == null) {
                return Collections.emptySet();
            }
            return n.get(key);
        }

        @Override
        public TrieNode<T> query(char c) {
            return data.get(c);
        }

        public Set<T> extract(String text) {
            Set<T> out = new HashSet<T>();
            TrieQuery<T> q = new TrieQuery<T>(this);
            int l = text.length();
            for (int i = 0; i < l; i++) {
                String t = text.substring(i);
                Set<T> found = null;
                for (char c : t.toCharArray()) {
                    q.query(c);
                    if (q.getStatus() == TrieQueryStatus.S_NO_MATCH) {
                        break;
                    }
                    if (q.getStatus() == TrieQueryStatus.S_EXACT_MATCH) {
                        found = q.getCurrentValues();
                    }
                }
                if (found != null) {
                    out.addAll(found);
                }
                q.restart();
            }
            return out;
        }

    }

    private enum LocationEntityType {
        COUNTRY, STATE, CITY, REGION
    }

    private final static class LocationEntity {

        LocationEntityType type;
        String name;
        LocationEntity parent;
        String areacode = null;
        String postcode = null;

        LocationEntity(LocationEntityType type, String name) {
            this(type, name, null);
        }

        LocationEntity(LocationEntityType type, String name, LocationEntity parent) {
            this.type = type;
            this.name = name.replaceAll("[　\\s]", "");
            this.parent = parent;
        }

        @Override
        public String toString() {
            return String.format("%s: %s", type, name);
        }
    }

    public final static class Address {

        private String country;
        private String state;
        private String city;
        private String region;
        private String postcode;
        private String areacode;

        public String getCountry() {
            return country;
        }

        public String getState() {
            return state;
        }

        public String getCity() {
            return city;
        }

        public String getRegion() {
            return region;
        }

        public String getAreaCode() {
            return areacode;
        }

        public String getPostcode() {
            return postcode;
        }

        @Override
        public String toString() {
            return String.format(
                    "Country: %s, State: %s, City: %s, Region: %s (postcode: %s, area code: %s)",
                    country, state, city, region, postcode, areacode);
        }

    }

}

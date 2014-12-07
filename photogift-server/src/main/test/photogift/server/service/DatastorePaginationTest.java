package photogift.server.service;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatastorePaginationTest extends TestCase {

    private static final String KEY = "__key__";
    private static final String KIND = "Person";
    private static final String PROPERTY_NAME = "initials";
    private static final int DATA_SIZE = 80;

    /**
     * Tests pagination of datastore entities.  Verifies forward and
     * backward paging returns entities in the exact same order, even
     * with duplicate values.
     *
     * @throws Exception
     */
    public void test() throws Exception {

        // Initialize some test entities.
        final List<Entity> entities = initializeTestData();

        // Add the entities to the datastore.
        DatastoreServiceFactory.getDatastoreService().put(entities);

        // Create a query to get the people, sorted by their initials.
        Query q = new Query(KIND);
        q.addSort(PROPERTY_NAME, SortDirection.ASCENDING);


        // This is to guarantee the order of duplicate initials.  This is not
        // necessary if you are not concerned with the order of the duplicates.
        q.addSort(KEY, SortDirection.ASCENDING);

        // Fetch with a page size of 30 people.
        final FetchOptions options = FetchOptions.Builder.withDefaults();
        options.limit(2);

        // Get first page.
        final Page page1 = getPage(q, options);
        System.out.println("Page 1");
        System.out.println(page1);

        // Get the next page by setting the cursor to the "next" cursor
        // from the current page.
        options.startCursor(page1.next);
        final Page page2 = getPage(q, options);
        System.out.println("Page 2");
        System.out.println(page2);

        // Make sure the next page starts after the previous page.
        assertTrue(page1.last().compareTo(page2.first()) < 0);

        // Get the next page by setting the cursor to the "next" cursor
        // from the current page.
        options.startCursor(page2.next);

        final Page page3 = getPage(q, options);
        System.out.println("Page 3");
        System.out.println(page3);

        // Make sure the next page starts after the previous page.
        assertTrue(page1.last().compareTo(page2.first()) < 0);

        // For paging backward, create the same query in the reverse order.
        // Of course, each page will create a new query according to the
        // page direction desired.
//        q = new Query(KIND);
//        q.addSort(PROPERTY_NAME, SortDirection.DESCENDING);
//        q.addSort(KEY, SortDirection.DESCENDING);

        // Get the previous page by setting the cursor to the "previous"
        // cursor from the current page.
//        assertEquals(page3.previous, page2.next);
        options.startCursor(page3.current);

        final Page page2a = getPage(q.reverse(), options);

        System.out.println("Page 2a");
        System.out.println(page2a);
        // As the page will be returned in the reverse order because the
        // query is reversed, reverse the page for comparison with the
        // original.
        System.out.println("Page 2a (reversed)");
        final Page reverse2a = page2a.rev();
        System.out.println(reverse2a);

        // Make sure this page 2 is exactly like the original.
        assertEquals(page2, reverse2a);

        // Get the previous page by setting the cursor to the "previous"
        // cursor from the current page.
        options.startCursor(page2a.next);
        final Page page1a = getPage(q, options);
        System.out.println("Page 1a");

        // As the page will be returned in the reverse order because the
        // query is reversed, reverse the page for comparison with the
        // original.
        final Page reverse1a = page1a.rev();
        System.out.println(reverse1a);

        // Make sure this page 1 is exactly like the original.
//        assertEquals(page1, reverse1a);
    }

    /**
     * Initializes a set of test data.  Added some duplicates around
     * the page breaks to test that out as well.
     *
     * @return the list of test people with initials.
     */
    private List<Entity> initializeTestData() {
        final List<Entity> entities = new ArrayList<Entity>();
        entities.add(entity("AA"));
        entities.add(entity("AB"));
        entities.add(entity("AC"));
        entities.add(entity("AD"));

        entities.add(entity("BA"));
        entities.add(entity("BB"));
        entities.add(entity("BC"));
        entities.add(entity("BD"));




//        Entity entity = null;
//
//        // Add some duplicates around the page break to
//        // see if it will mess things up.
//        for (int i = 0; i < 4; i++) {
//            entity = new Entity(KIND);
//            entity.setProperty(PROPERTY_NAME, "BD");
//            entities.add(entity);
//        }
//
//        // Add more duplicates around the page break to
//        // see if it will mess things up.
//        for (int i = 0; i < 4; i++) {
//            entity = new Entity(KIND);
//            entity.setProperty(PROPERTY_NAME, "BC");
//            entities.add(entity);
//        }
//
//        // Create a bunch of Person entities with initials.
//        for (int i = 0; i < DATA_SIZE; i++) {
//            final char[] chars = new char[2];
//            final int j = (i / 26);
//
//            chars[0] = (char) ('A' + (j % 26));
//            chars[1] = (char) ('A' + (i % 26));
//
//            final String s = new String(chars);
//
//            entity = new Entity(KIND);
//            entity.setProperty(PROPERTY_NAME, s);
//            entities.add(entity);
//        }

        return entities;
    }

    Entity entity(String property) {
        Entity entity = new Entity(KIND);
        entity.setProperty(PROPERTY_NAME, property);
        return entity;
    }

    /**
     * Create a page object to hold the results of the query.
     *
     * @param query   - the query.
     * @param options - the fetch option.
     * @return the page representation of the query.
     */
    private Page getPage(final Query query, final FetchOptions options) {

        final DatastoreService data = DatastoreServiceFactory.getDatastoreService();

        final PreparedQuery p = data.prepare(query);

//        p.countEntities(options);

        final QueryResultIterator<Entity> iterator = p.asQueryResultIterator(options);

        final List<String> list = new ArrayList<String>();
        Cursor start = null;

        if (start == null) {
            start = iterator.getCursor();
        }

        while (iterator.hasNext()) {
//            System.out.println(iterator.getCursor().toWebSafeString());
            final Entity e = iterator.next();

            // Sometimes I use String keys, so that needs to be
            // supported here.
            String id = e.getKey().getName();
            if (id == null) {
                id = Long.toString(e.getKey().getId());
            }

            // Get the property name we are sorting on so we can
            // grab its value.
            final String propertyName = query.getSortPredicates().get(0).getPropertyName();
            final Object property = e.getProperty(propertyName);

            // Tack on the id so we can verify the order of the entities.
            list.add(property + "-" + id);

            // Set the start cursor to the first entity retrieved.

        }

        // Set the end cursor to the last entity retrieved.
        final Cursor end = iterator.getCursor();

        // Create a new page to return.
        final Page page = new Page(list, start, end);
        return page;
    }

    /**
     * Represents a page returned from the datastore.
     */
    public class Page {
        private final List<String> content;
        private final Cursor current;
        private final Cursor next;

        public Page(final List<String> content, final Cursor current, final Cursor next) {
            this.content = content;
            this.current = current;
            this.next = next;
        }

        /**
         * @return the first content entity.
         */
        public String first() {
            if (content == null) {
                return null;
            }
            return content.get(0);
        }

        /**
         * @return the last content entity.
         */
        public String last() {
            if (content == null) {
                return null;
            }
            return content.get(content.size() - 1);
        }

        @Override
        public int hashCode() {
            return toString().hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }

            return this.toString().equals(obj.toString());
        }

        /**
         * Format the page for printing.  Display the page contents
         * as well as the cursors for previous and next.
         */
        @Override
        public String toString() {
            return new StringBuilder()
                    .append("content: ")
                    .append(content)
                    .append('\n')
                    .append("prev: ")
                    .append(getIndex(current))
                    .append('\n')
                    .append("next: ")
                    .append(getIndex(next))
                    .append('\n')
                    .toString();
        }

        /**
         * The cursors print as a structured string.  The relevant
         * information is the stringValue of the record indexed.
         * Grab that value for less verbose output when printing.
         *
         * @param cursor
         * @return the value of the record indexed.
         */
        public String getIndex(final Cursor cursor) {

            if (cursor == null) return null;
//            final String string = cursor.toString();
//            if (Strings.isNullOrEmpty(string)) {
//                return "";
//            }
//            final String label = "stringValue: ";
//            final int start = string.indexOf(label);
//            final int end = string.indexOf('\n', start);
//            final String name = string.substring(start + label.length(), end);
            return cursor.toWebSafeString();
        }

        /**
         * If the page was retrieved with the reverse query, the
         * page contents and cursors will be in reverse order.
         * Reverse the page for comparisons with the original.
         *
         * @return the reversed page.
         */
        public Page rev() {
            final ArrayList<String> copy = new ArrayList<String>(content);
            Collections.reverse(copy);

            final Page reversed = new Page(copy, next, current);
            return reversed;
        }
    }

    /**
     * Configure the test with the datastore helper.
     */
    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    @Override
    @Before
    public void setUp() throws Exception {
        helper.setUp();
    }

    @Override
    @After
    public void tearDown() {
        helper.tearDown();
    }
}
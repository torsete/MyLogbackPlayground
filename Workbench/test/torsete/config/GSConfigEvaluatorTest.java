package torsete.config;

import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;

/**
 * Created by Torsten on 01.03.2018.
 */
public class GSConfigEvaluatorTest {


    @Test
    public void testGetValues() throws IOException {
        Map<String, String> map;
        Map<String, String> result;
        Properties properties;
        List<String> values;
        int i;
        GSConfigEvaluator evaluator = new GSConfigEvaluator();

        String sourceString = "" +
                "a0=" + "\n" +
                "b0=1" + "\n" +
                "c0=2,3" + "\n" +
                "d0=4,5,6" + "\n" +
                "e0=," + "\n" +
                "f0=7," + "\n" +
                "g0=,8" + "\n" +
                "h0=,," + "\n" +
                "i0=,9," + "\n" +
                "" + "\n" +
                "a1={" + "\n" +
                "b1={1" + "\n" +
                "c1={2,3" + "\n" +
                "d1={4,5,6" + "\n" +
                "e1={," + "\n" +
                "f1={7," + "\n" +
                "g1={,8" + "\n" +
                "h1={,," + "\n" +
                "i1={,9," + "\n" +
                "" + "\n" +
                "a2=}" + "\n" +
                "b2=1}" + "\n" +
                "c2=2,3}" + "\n" +
                "d2=4,5,6}" + "\n" +
                "e2=,}" + "\n" +
                "f2=7,}" + "\n" +
                "g2=,8}" + "\n" +
                "h2=,,}" + "\n" +
                "i2=,9,}" + "\n" +
                "" + "\n" +
                "a3={}" + "\n" +
                "b3={1}" + "\n" +
                "c3={2,3}" + "\n" +
                "d3={4,5,6}" + "\n" +
                "e3={,}" + "\n" +
                "f3={7,}" + "\n" +
                "g3={,8}" + "\n" +
                "h3={,,}" + "\n" +
                "i3={,9,}" + "\n" +
                "" + "\n" +
                "";

        properties = new Properties();
        properties.load(new StringReader(sourceString));
        map = new HashMap<>();
        Iterator<Map.Entry<Object, Object>> iterator = properties.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Object, Object> e = iterator.next();
            map.put(e.getKey().toString(), e.getValue().toString());
        }
        result = evaluator.apply(map);
        result.entrySet().forEach(e -> System.out.println(e));

        assertNull(evaluator.getValue("xxxx"));
        assertEquals("", evaluator.getValue("a0"));
        assertEquals(",9,", evaluator.getValue("i0"));

        assertNull(evaluator.getValues("xxxx"));

        for (int keySuffix = 0; keySuffix < 4; keySuffix++) {

            i = 0;
            values = evaluator.getValues("a" + keySuffix);
            assertEquals(1, values.size());
            assertEquals("", values.get(i++));

            i = 0;
            values = evaluator.getValues("b" + keySuffix);
            assertEquals(1, values.size());
            assertEquals("1", values.get(i++));

            i = 0;
            values = evaluator.getValues("c" + keySuffix);
            assertEquals("keySuffix=" + keySuffix, 2, values.size());
            assertEquals("2", values.get(i++));
            assertEquals("3", values.get(i++));

            i = 0;
            values = evaluator.getValues("d" + keySuffix);
            assertEquals(3, values.size());
            assertEquals("4", values.get(i++));
            assertEquals("5", values.get(i++));
            assertEquals("6", values.get(i++));

            i = 0;
            values = evaluator.getValues("e" + keySuffix);
            assertEquals(2, values.size());
            assertEquals("", values.get(i++));
            assertEquals("", values.get(i++));

            i = 0;
            values = evaluator.getValues("f" + keySuffix);
            assertEquals(2, values.size());
            assertEquals("7", values.get(i++));
            assertEquals("", values.get(i++));

            i = 0;
            values = evaluator.getValues("g" + keySuffix);
            assertEquals(2, values.size());
            assertEquals("", values.get(i++));
            assertEquals("8", values.get(i++));

            i = 0;
            values = evaluator.getValues("h" + keySuffix);
            assertEquals(3, values.size());
            assertEquals("", values.get(i++));
            assertEquals("", values.get(i++));
            assertEquals("", values.get(i++));

            i = 0;
            values = evaluator.getValues("i" + keySuffix);
            assertEquals(3, values.size());
            assertEquals("", values.get(i++));
            assertEquals("9", values.get(i++));
            assertEquals("", values.get(i++));
        }

    }

    @Test
    public void testVariables1() throws IOException {
        Map<String, String> map;
        Map<String, String> result;
        Properties properties;
        List<String> values;
        int i;
        GSConfigEvaluator evaluator = new GSConfigEvaluator();

        String sourceString = "" +
                "a=" + "\n" +
                "b=1" + "\n" +
                "c=2,3" + "\n" +
                "d=4,5,6" + "\n" +
                "e=," + "\n" +
                "f=7," + "\n" +
                "g=,8" + "\n" +
                "h=,," + "\n" +
                "i=,9," + "\n" +
                "" + "\n" +
                "res_=$$" + "\n" +
                "res_a=$a$" + "\n" +
                "res_b=$b$" + "\n" +
                "res_c=$c$" + "\n" +
                "res_d=$d$" + "\n" +
                "res_e=$e$" + "\n" +
                "res_f=$f$" + "\n" +
                "res_g=$g$" + "\n" +
                "res_h=$h$" + "\n" +
                "res_i=$i$" + "\n" +
                "" + "\n" +
                "";

        properties = new Properties();
        properties.load(new StringReader(sourceString));
        map = new HashMap<>();
        Iterator<Map.Entry<Object, Object>> iterator = properties.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Object, Object> e = iterator.next();
            map.put(e.getKey().toString(), e.getValue().toString());
        }
        result = evaluator.apply(map);
        result.entrySet().forEach(e -> System.out.println(e));

        assertEquals("1", evaluator.getValue("b"));
        assertEquals("1", evaluator.getValue("res_b"));
        for (char c = 'a'; c <= 'i'; c++) {
            assertEquals(evaluator.getValue(String.valueOf(c)), evaluator.getValue("res_" + String.valueOf(c)));
        }


    }

    @Test
    public void testVariables2() throws IOException {
        Map<String, String> map;
        Map<String, String> result;
        Properties properties;
        List<String> values;
        int i;
        GSConfigEvaluator evaluator = new GSConfigEvaluator();

        String sourceString = "" +
                "a=" + "\n" +
                "b=1" + "\n" +
                "c=2,3" + "\n" +
                "res_a_b=$a$,$b$" + "\n" +
                "res_b_c=$b$,$c$" + "\n" +
                "" + "\n" +
                "res_res_b_c_c=$res_b_c$,$c$" + "\n" +
                "" + "\n" +
                "" + "\n" +
                "";

        properties = new Properties();
        properties.load(new StringReader(sourceString));
        map = new HashMap<>();
        Iterator<Map.Entry<Object, Object>> iterator = properties.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Object, Object> e = iterator.next();
            map.put(e.getKey().toString(), e.getValue().toString());
        }
        result = evaluator.apply(map);
        result.entrySet().forEach(e -> System.out.println(e));

        i = 0;
        values = evaluator.getValues("res_a_b");
        assertEquals(2, values.size());
        assertEquals("", values.get(i++));
        assertEquals("1", values.get(i++));

        i = 0;
        values = evaluator.getValues("res_b_c");
        assertEquals(3, values.size());
        assertEquals("1", values.get(i++));
        assertEquals("2", values.get(i++));
        assertEquals("3", values.get(i++));

        i = 0;
        values = evaluator.getValues("res_res_b_c_c");
        assertEquals(5, values.size());
        assertEquals("1", values.get(i++));
        assertEquals("2", values.get(i++));
        assertEquals("3", values.get(i++));
        assertEquals("2", values.get(i++));
        assertEquals("3", values.get(i++));


    }

    @Test
    public void testVariables3() throws IOException {
        Map<String, String> map;
        Map<String, String> result;
        Properties properties;
        List<String> values;
        int i;
        GSConfigEvaluator evaluator = new GSConfigEvaluator();

        String sourceString = "" +
                "a=$a$" + "\n" +
                "b=$c$" + "\n" +
                "c=$b$" + "\n" +
                "k0=$k1$" + "\n" +
                "k1=$k2$" + "\n" +
                "k2=$k0$" + "\n" +
                "";

        properties = new Properties();
        properties.load(new StringReader(sourceString));
        map = new HashMap<>();
        Iterator<Map.Entry<Object, Object>> iterator = properties.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Object, Object> e = iterator.next();
            map.put(e.getKey().toString(), e.getValue().toString());
        }
        result = evaluator.apply(map);
        result.entrySet().forEach(e -> System.out.println(e));

        i = 0;
        values = evaluator.getValues("a");
        assertEquals(1, values.size());
        assertEquals("$a$", values.get(i++));

        i = 0;
        values = evaluator.getValues("b");
        assertEquals(1, values.size());
        assertEquals("$c$", values.get(i++));

        i = 0;
        values = evaluator.getValues("c");
        assertEquals(1, values.size());
        assertEquals("$b$", values.get(i++));

        i = 0;
        values = evaluator.getValues("k0");
        assertEquals(1, values.size());
        assertEquals("$k1$", values.get(i++));

        i = 0;
        values = evaluator.getValues("k1");
        assertEquals(1, values.size());
        assertEquals("$k2$", values.get(i++));

        i = 0;
        values = evaluator.getValues("k2");
        assertEquals(1, values.size());
        assertEquals("$k0$", values.get(i++));


    }

    @Test
    public void testSettings() throws IOException {
        Map<String, String> map;
        Map<String, String> result;
        Properties properties;
        List<String> values;
        int i;
        GSConfigEvaluator evaluator = new GSConfigEvaluator();

//        arrayDelimiter = getValue(inputMap, "-arrayDelimiter", arrayDelimiter);
//        arrayPrefix = getValue(inputMap, "-arrayPrefix", arrayPrefix);
//        arraySuffix = getValue(inputMap, "-arraySuffix", arraySuffix);
//        variablePrefix = getValue(inputMap, "-variablePrefix", variablePrefix);
//        variableSuffix = getValue(inputMap, "-variableSuffix", variableSuffix);


        String sourceString = "" +
                "-arrayDelimiter=////" + "\n" +
                "-arrayPrefix=[[" + "\n" +
                "-arraySuffix=]]" + "\n" +
                "-variablePrefix=>>>>" + "\n" +
                "-variableSuffix=<1<" + "\n" +
                "" + "\n" +
                "a=1" + "\n" +
                "" + "\n" +
                "b=>>>>a<1<" + "\n" +
                "" + "\n" +
                "c=[[2////3]]" + "\n" +
                "" + "\n" +
                "" + "\n" +
                "";

        properties = new Properties();
        properties.load(new StringReader(sourceString));
        map = new HashMap<>();
        Iterator<Map.Entry<Object, Object>> iterator = properties.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Object, Object> e = iterator.next();
            map.put(e.getKey().toString(), e.getValue().toString());
        }
        result = evaluator.apply(map);
        result.entrySet().forEach(e -> System.out.println(e));

        assertEquals("1", evaluator.getValue("b"));
        i = 0;
        values = evaluator.getValues("c");
        assertEquals(2, values.size());
        assertEquals("2", values.get(i++));
        assertEquals("3", values.get(i++));


    }
}

package info.blockchain.wallet.test_data;

/**
 * Created by riaanvos on 25/04/16.
 */
public class UnspentTestData {

    public static String ADDRESS = "19Axrcn8nsdZkSJtbnyM1rCs1PGwSzzzNn";
    public static String NOTICE = "Some funds are pending confirmation and cannot be spent yet (Value 0.001 BTC)";
    public static int UNSPENT_OUTPUTS_COUNT = 8;
    public static long BALANCE = 360200l;

    /*
    8 available coins: [80200,70000,60000,50000,40000,30000,20000,10000]
     */
    public static String apiResponseString = "{\n" +
            "\t\"unspent_outputs\": [{\n" +
            "\t\t\"tx_hash\": \"8b024eb5446a31c058cb75c33d05e4f6412d83596692c3fb977ce01f6bee4540\",\n" +
            "\t\t\"tx_hash_big_endian\": \"4045ee6b1fe07c97fbc3926659832d41f6e4053dc375cb58c0316a44b54e028b\",\n" +
            "\t\t\"tx_index\": 136339683,\n" +
            "\t\t\"tx_output_n\": 0,\n" +
            "\t\t\"script\": \"76a91459a3602f1dc9fd3ff771e21883dedf18e11107cf88ac\",\n" +
            "\t\t\"value\": 80200,\n" +
            "\t\t\"value_hex\": \"4e20\",\n" +
            "\t\t\"confirmations\": 4228\n" +
            "\t}, {\n" +
            "\t\t\"tx_hash\": \"e6e8df9ab5cbf8eb8f0586e8f15db1ad0049a1b080e9dd2a4c2191ee33f1f0e6\",\n" +
            "\t\t\"tx_hash_big_endian\": \"e6f0f133ee91214c2adde980b0a14900adb15df1e886058febf8cbb59adfe8e6\",\n" +
            "\t\t\"tx_index\": 138429831,\n" +
            "\t\t\"tx_output_n\": 0,\n" +
            "\t\t\"script\": \"76a91459a3602f1dc9fd3ff771e21883dedf18e11107cf88ac\",\n" +
            "\t\t\"value\": 70000,\n" +
            "\t\t\"value_hex\": \"7530\",\n" +
            "\t\t\"confirmations\": 2607\n" +
            "\t}, {\n" +
            "\t\t\"tx_hash\": \"1080f68e3290b7fdd806015ed7bfe066b5c77349b50014582b414c277ceff1ac\",\n" +
            "\t\t\"tx_hash_big_endian\": \"acf1ef7c274c412b581400b54973c7b566e0bfd75e0106d8fdb790328ef68010\",\n" +
            "\t\t\"tx_index\": 138429858,\n" +
            "\t\t\"tx_output_n\": 0,\n" +
            "\t\t\"script\": \"76a91459a3602f1dc9fd3ff771e21883dedf18e11107cf88ac\",\n" +
            "\t\t\"value\": 60000,\n" +
            "\t\t\"value_hex\": \"2710\",\n" +
            "\t\t\"confirmations\": 2607\n" +
            "\t}, {\n" +
            "\t\t\"tx_hash\": \"d0c489434aa1c4ae02d49f1e4c2ca5a8513d8d83a139ce1536102013a914f974\",\n" +
            "\t\t\"tx_hash_big_endian\": \"74f914a91320103615ce39a1838d3d51a8a52c4c1e9fd402aec4a14a4389c4d0\",\n" +
            "\t\t\"tx_index\": 138446227,\n" +
            "\t\t\"tx_output_n\": 0,\n" +
            "\t\t\"script\": \"76a914095ca2924c0a184d1f48e209c79f9c0076a7b7a088ac\",\n" +
            "\t\t\"value\": 50000,\n" +
            "\t\t\"value_hex\": \"0186a0\",\n" +
            "\t\t\"confirmations\": 2592\n" +
            "\t}, {\n" +
            "\t\t\"tx_hash\": \"21ef1884ad111f9b520062332c24d0dc0693c690c0983fdd22bbc5d9b50bde8e\",\n" +
            "\t\t\"tx_hash_big_endian\": \"8ede0bb5d9c5bb22dd3f98c090c69306dcd0242c336200529b1f11ad8418ef21\",\n" +
            "\t\t\"tx_index\": 138446297,\n" +
            "\t\t\"tx_output_n\": 0,\n" +
            "\t\t\"script\": \"76a91459a3602f1dc9fd3ff771e21883dedf18e11107cf88ac\",\n" +
            "\t\t\"value\": 40000,\n" +
            "\t\t\"value_hex\": \"013880\",\n" +
            "\t\t\"confirmations\": 2590\n" +
            "\t}, {\n" +
            "\t\t\"tx_hash\": \"9d71d824de15afe1827ef5880f1e090efa8cdfd0db4c6c18091ab24dbb156d55\",\n" +
            "\t\t\"tx_hash_big_endian\": \"556d15bb4db21a09186c4cdbd0df8cfa0e091e0f88f57e82e1af15de24d8719d\",\n" +
            "\t\t\"tx_index\": 139010453,\n" +
            "\t\t\"tx_output_n\": 0,\n" +
            "\t\t\"script\": \"76a91459a3602f1dc9fd3ff771e21883dedf18e11107cf88ac\",\n" +
            "\t\t\"value\": 30000,\n" +
            "\t\t\"value_hex\": \"4e20\",\n" +
            "\t\t\"confirmations\": 2144\n" +
            "\t}, {\n" +
            "\t\t\"tx_hash\": \"fcf6b6e0ea69d0b809aae5b971b4583ee8334e7ffbe2489e2a0bdc302af3fe0f\",\n" +
            "\t\t\"tx_hash_big_endian\": \"0ffef32a30dc0b2a9e48e2fb7f4e33e83e58b471b9e5aa09b8d069eae0b6f6fc\",\n" +
            "\t\t\"tx_index\": 139010492,\n" +
            "\t\t\"tx_output_n\": 0,\n" +
            "\t\t\"script\": \"76a914095ca2924c0a184d1f48e209c79f9c0076a7b7a088ac\",\n" +
            "\t\t\"value\": 20000,\n" +
            "\t\t\"value_hex\": \"4c20\",\n" +
            "\t\t\"confirmations\": 2144\n" +
            "\t}, {\n" +
            "\t\t\"tx_hash\": \"4a318c23ed31ae0dd6510fed0dd15d96d227ffce1c9dc7c5aa0adc616475aaeb\",\n" +
            "\t\t\"tx_hash_big_endian\": \"ebaa756461dc0aaac5c79d1cceff27d2965dd10ded0f51d60dae31ed238c314a\",\n" +
            "\t\t\"tx_index\": 139025936,\n" +
            "\t\t\"tx_output_n\": 0,\n" +
            "\t\t\"script\": \"76a91459a3602f1dc9fd3ff771e21883dedf18e11107cf88ac\",\n" +
            "\t\t\"value\": 10000,\n" +
            "\t\t\"value_hex\": \"2710\",\n" +
            "\t\t\"confirmations\": 2133\n" +
            "\t}],\n" +
            "\"notice\": \"Some funds are pending confirmation and cannot be spent yet (Value 0.001 BTC)\"\n" +
            "}";
}

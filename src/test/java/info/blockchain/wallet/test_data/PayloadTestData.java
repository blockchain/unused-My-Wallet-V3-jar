package info.blockchain.wallet.test_data;

public class PayloadTestData {

    public static final String SHARED_KEY = "a8ad154a-f4fa-4e4d-9cb0-87f079ccbb06";
    public static final String GUID = "60e5c6aa-2969-4130-9143-b43a1f5ddac7";
    public static final int PDKDF2_ITERATIONS = 5000;
    public static final int DEFAULT_ACCOUNT_INDEX = 3;
    public static final String MNEMONIC_VERIFIED = "true";
    public static final String DPASSWORD_HASH = "d11731d47e88b47fac19fc5346956532dbbe0e28883411084287f9ead9aedde8";
    public static final String DOUBLE_ENCRYPTED = "true";
    public static final int TOTAL_ACCOUNTS = 3;
    public static final int TOTAL_LEGACY_ADDRESSES = 2;
    public static final int TOTAL_ADDRESSBOOK_ENTRIES = 1;

    public static final String HTML_NOTIFICATIONS = "true";

    public static final String ACCOUNT_1_XPRIV = "xpriv1t0wMM8OQCuLCx5b0BiBCFIkProCyDK7MOLr+ff8tJQGgbDqeVQ/g/f4S9jUiSg3XlrmLDfCIpklpy2g+aLnTnnj2p+lhgAWKdJqhdjMMPyl7RqduxGMTQ4N9WTyNXyHIUr6ijwGRM29UtUt";
    public static final String ACCOUNT_2_XPRIV = "xpriv2t0wMM8OQCuLCx5b0BiBCFIkProCyDK7MOLr+ff8tJQGgbDqeVQ/g/f4S9jUiSg3XlrmLDfCIpklpy2g+aLnTnnj2p+lhgAWKdJqhdjMMPyl7RqduxGMTQ4N9WTyNXyHIUr6ijwGRM29UtUt";
    public static final String ACCOUNT_3_XPRIV = "xpriv3t0wMM8OQCuLCx5b0BiBCFIkProCyDK7MOLr+ff8tJQGgbDqeVQ/g/f4S9jUiSg3XlrmLDfCIpklpy2g+aLnTnnj2p+lhgAWKdJqhdjMMPyl7RqduxGMTQ4N9WTyNXyHIUr6ijwGRM29UtUt";

    public static final String ACCOUNT_1_XPUB = "xpub16C3GhPc6DFFkMSJEX1fRW6EJ8UvEoonx4cMDcpY1iYoCAmwmpnBDmXuRH99bi7pcE2SEhBu4DJcZJFBb5uUtKpHt6qy4TPs6LuYFZ7oiqa8";
    public static final String ACCOUNT_2_XPUB = "xpub26C3GhPc6DFFkMSJEX1fRW6EJ8UvEoonx4cMDcpY1iYoCAmwmpnBDmXuRH99bi7pcE2SEhBu4DJcZJFBb5uUtKpHt6qy4TPs6LuYFZ7oiqa8";
    public static final String ACCOUNT_3_XPUB = "xpub36C3GhPc6DFFkMSJEX1fRW6EJ8UvEoonx4cMDcpY1iYoCAmwmpnBDmXuRH99bi7pcE2SEhBu4DJcZJFBb5uUtKpHt6qy4TPs6LuYFZ7oiqa8";

    public static final String ACCOUNT_1_LABEL = "Label 1";
    public static final String ACCOUNT_2_LABEL = "Long label is long. So so long";
    public static final String ACCOUNT_3_LABEL = "!@&^£$><?/:}{";

    public static final String ACCOUNT_1_ARCHIVED = "true";
    public static final String ACCOUNT_2_ARCHIVED = "false";
    public static final String ACCOUNT_3_ARCHIVED = "false";

    public static final String HD_WALLET_SEED_HEX = "kXl6UMJuJJHD9wu6cz7U4DwMnT2aKfGwMDzquNwTW8UgxXJoGxZuMckAK4SCstjdYPoIimfyHbB3zzcHJz9mRw==";

    public static final String ADDRESS_1_LABEL = "Weird chars !£@%@*&^!@)(&!£:<>?";

    public static final String ADDRESS_1_KEY = "nkdmAFsgDNT1mVzPm7GGj6+Z4sL7gnY/PxrWFe6Yrj8hzbHOik1ZyVes1Ryz5NTUkodnu6Py8LuqKD3a6jQ1WA==";
    public static final String ADDRESS_1_ADDRESS = "13PRUmsANWasdfcjMUqA3TGpmUFdw1SksX";

    public static final String ADDRESS_2_KEY = "nkdmAFsgDNT1mVzPm7GGj6+Z4sL7gnY/PxrWFe6Yrj8hzbHOik1ZyVes1Ryz5NTUkodnu6Py8LuqKD3a6jQ1WA==";
    public static final String ADDRESS_2_ADDRESS = "13PRUmsANWasdfa89b7ydi8a6dGpmUFdw1SksX";

    public static final String ADDRESSBOOK_1_LABEL = "Dat Label";
    public static final String ADDRESSBOOK_1_ADDRESS = "13Pakdsv68ias6dka8a6dGpmUFdw1SksX";

    public static String jsonObject = "{\n" +
            "    \"sharedKey\": \"" + SHARED_KEY + "\",\n" +
            "    \"pbkdf2_iterations\": " + PDKDF2_ITERATIONS + ",\n" +
            "    \"paidTo\": {\n" +
            "        \n" +
            "    },\n" +
            "    \"hd_wallets\": [\n" +
            "        {\n" +
            "            \"default_account_idx\": " + DEFAULT_ACCOUNT_INDEX + ",\n" +
            "            \"passphrase\": \"\",\n" +
            "            \"mnemonic_verified\": " + MNEMONIC_VERIFIED + ",\n" +
            "            \"accounts\": [\n" +
            "                {\n" +
            "                    \"archived\": " + ACCOUNT_1_ARCHIVED + ",\n" +
            "                    \"cache\": {\n" +
            "                        \"changeAccount\": \"xpub6EBxAYdVUZnnj1NHhvKr2EKMm6iUderniUiRhECMcZebSLJWHffpZ7hXsXqc1dLfygotLCf9Mj8o9MFjN7E93aMLDf5Zj9j7uDzg6nn5e2t\",\n" +
            "                        \"receiveAccount\": \"xpub6EBxAYdVUZnnfPrjjpH9YwD11WQLRu7Eg5UKvUAkU6HyTzYmWm4gGc5ZZW5HdH7SEvvu5oXRYf36jX5iT6twCDt9gdmicHSRReumho45jKr\"\n" +
            "                    },\n" +
            "                    \"xpriv\": \"" + ACCOUNT_1_XPRIV + "\",\n" +
            "                    \"address_labels\": [\n" +
            "                        \n" +
            "                    ],\n" +
            "                    \"label\": \"" + ACCOUNT_1_LABEL + "\",\n" +
            "                    \"xpub\": \"" + ACCOUNT_1_XPUB + "\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"archived\": " + ACCOUNT_2_ARCHIVED + ",\n" +
            "                    \"cache\": {\n" +
            "                        \"changeAccount\": \"xpub6EcLdGfU3ohNd4fUWaWVeKX4HagZoAVHbLkpRoJKcRtsXtwzDwp7KLCQsXfSUznbd17xC4iaaPzvZePLWp6bCsjsTWAGrPdVdGhsoPMVL6Z\",\n" +
            "                        \"receiveAccount\": \"xpub6EcLdGfU3ohNa59ixebeKN5rEHSwMJuNjj4QRLLdsbK3NJyK9Yxkz8AtT4CGYbXRZFr63N8gMrt4fja7E6UE5cjMnJzdfqGoeWyN7HTjARi\"\n" +
            "                    },\n" +
            "                    \"xpriv\": \"" + ACCOUNT_2_XPRIV + "\",\n" +
            "                    \"address_labels\": [\n" +
            "                        \n" +
            "                    ],\n" +
            "                    \"label\": \"" + ACCOUNT_2_LABEL + "\",\n" +
            "                    \"xpub\": \"" + ACCOUNT_2_XPUB + "\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"archived\": " + ACCOUNT_3_ARCHIVED + ",\n" +
            "                    \"cache\": {\n" +
            "                        \"changeAccount\": \"xpub6F1W47aKNe3fAHU38uwDH5nWy4YvjTARoeXwnoSQZVcyYY2KiJ3wSEVpzPiZQCpYYKqEviNvU9XXHJryxeA77njbpP4L8GBcLXMXVuZt6T5\",\n" +
            "                        \"receiveAccount\": \"xpub6F1W47aKNe3f7btFQvdvLAZdWJfzDAX3GBmNBsRTs3Uo32s3tj1pWVbXhnJTqs4tz2jsUkSRR3fW3nxcLVoyf8PRswLJc8WsB1VFbSDYbj3\"\n" +
            "                    },\n" +
            "                    \"xpriv\": \"" + ACCOUNT_3_XPRIV + "\",\n" +
            "                    \"address_labels\": [\n" +
            "                        \n" +
            "                    ],\n" +
            "                    \"label\": \"" + ACCOUNT_3_LABEL + "\",\n" +
            "                    \"xpub\": \"" + ACCOUNT_3_XPUB + "\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"seed_hex\": \"" + HD_WALLET_SEED_HEX + "\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"keys\": [\n" +
            "        {\n" +
            "            \"created_time\": 1427898445750,\n" +
            "            \"created_device_version\": \"1.0\",\n" +
            "            \"priv\": \"" + ADDRESS_1_KEY + "\",\n" +
            "            \"tag\": 0,\n" +
            "            \"label\": \"" + ADDRESS_1_LABEL + "\",\n" +
            "            \"addr\": \"" + ADDRESS_1_ADDRESS + "\",\n" +
            "            \"created_device_name\": \"javascript_web\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"created_time\": 1430992239,\n" +
            "            \"created_device_version\": null,\n" +
            "            \"priv\": \"" + ADDRESS_2_KEY + "\",\n" +
            "            \"tag\": 0,\n" +
            "            \"addr\": \"" + ADDRESS_2_ADDRESS + "\",\n" +
            "            \"created_device_name\": \"android\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"dpasswordhash\": \"" + DPASSWORD_HASH + "\",\n" +
            "    \"double_encryption\": " + DOUBLE_ENCRYPTED + ",\n" +
            "    \"address_book\": [\n" +
            "        {\n" +
            "            \"label\": \"" + ADDRESSBOOK_1_LABEL + "\",\n" +
            "            \"addr\": \"" + ADDRESSBOOK_1_ADDRESS + "\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"tag_names\": [\n" +
            "        \n" +
            "    ],\n" +
            "    \"tx_notes\": {\n" +
            "        \n" +
            "    },\n" +
            "    \"options\": {\n" +
            "        \"pbkdf2_iterations\": " + PDKDF2_ITERATIONS + ",\n" +
            "        \"html5_notifications\": " + HTML_NOTIFICATIONS + ",\n" +
            "        \"logout_time\": 600000,\n" +
            "        \"enable_multiple_accounts\": true,\n" +
            "        \"fee_per_kb\": 10000\n" +
            "    },\n" +
            "    \"guid\": \"" + GUID + "\",\n" +
            "    \"tx_tags\": {\n" +
            "        \n" +
            "    }\n" +
            "}";

    /*
    No tx_tags
    No tag_names
    No tx_notes
    No paidTo
    No address_book
    No options
    No second password
    No iterations
     */
    public static String jsonObject_minimal = "{\n" +
            "    \"sharedKey\": \"" + SHARED_KEY + "\",\n" +
            "    \"hd_wallets\": [\n" +
            "        {\n" +
            "            \"default_account_idx\": " + DEFAULT_ACCOUNT_INDEX + ",\n" +
            "            \"passphrase\": \"\",\n" +
            "            \"mnemonic_verified\": " + MNEMONIC_VERIFIED + ",\n" +
            "            \"accounts\": [\n" +
            "                {\n" +
            "                    \"archived\": false,\n" +
            "                    \"cache\": {\n" +
            "                        \"changeAccount\": \"xpub6EBxAYdVUZnnj1NHhvKr2EKMm6iUderniUiRhECMcZebSLJWHffpZ7hXsXqc1dLfygotLCf9Mj8o9MFjN7E93aMLDf5Zj9j7uDzg6nn5e2t\",\n" +
            "                        \"receiveAccount\": \"xpub6EBxAYdVUZnnfPrjjpH9YwD11WQLRu7Eg5UKvUAkU6HyTzYmWm4gGc5ZZW5HdH7SEvvu5oXRYf36jX5iT6twCDt9gdmicHSRReumho45jKr\"\n" +
            "                    },\n" +
            "                    \"xpriv\": \"yDbhhb+t0wMM8OQCuLCx5b0BiBCFIkProCyDK7MOLr+ff8tJQGgbDqeVQ/g/f4S9jUiSg3XlrmLDfCIpklpy2g+aLnTnnj2p+lhgAWKdJqhdjMMPyl7RqduxGMTQ4N9WTyNXyHIUr6ijwGRM29UtUt+Gu3ABHGPBsDwUnKsEjZA=\",\n" +
            "                    \"address_labels\": [\n" +
            "                        \n" +
            "                    ],\n" +
            "                    \"label\": \"Savings\",\n" +
            "                    \"xpub\": \"xpub6C3GhPc6DFFkMSJEX1fRW6EJ8UvEoonx4cMDcpY1iYoCAmwmpnBDmXuRH99bi7pcE2SEhBu4DJcZJFBb5uUtKpHt6qy4TPs6LuYFZ7oiqa8\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"seed_hex\": \"kXl6UMJuJJHD9wu6cz7U4DwMnT2aKfGwMDzquNwTW8UgxXJoGxZuMckAK4SCstjdYPoIimfyHbB3zzcHJz9mRw==\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"keys\": [\n" +
            "        {\n" +
            "            \"created_time\": 1427898445750,\n" +
            "            \"created_device_version\": \"1.0\",\n" +
            "            \"priv\": \"nkdmAFsgDNT1mVzPm7GGj6+Z4sL7gnY/PxrWFe6Yrj8hzbHOik1ZyVes1Ryz5NTUkodnu6Py8LuqKD3a6jQ1WA==\",\n" +
            "            \"tag\": 0,\n" +
            "            \"label\": \"Weird chars !£@%@*&^!@)(&!£\\\":<>?\",\n" +
            "            \"addr\": \"13PRUmsANWm7sdcjMUqA3TGpmUFdw1SksX\",\n" +
            "            \"created_device_name\": \"javascript_web\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"guid\": \"" + GUID + "\"\n" +
            "}";

    /*
    Null legacy address
     */
    public static String jsonObject_CorruptLegacyAddress = "{\n" +
            "    \"sharedKey\": \"" + SHARED_KEY + "\",\n" +
            "    \"hd_wallets\": [],\n" +
            "    \"keys\": [\n" +
            "        {\n" +
            "            \"created_time\": 1427898445750,\n" +
            "            \"created_device_version\": \"1.0\",\n" +
            "            \"priv\": \"nkdmAFsgDNT1mVzPm7GGj6+Z4sL7gnY/PxrWFe6Yrj8hzbHOik1ZyVes1Ryz5NTUkodnu6Py8LuqKD3a6jQ1WA==\",\n" +
            "            \"tag\": 0,\n" +
            "            \"label\": \"Weird chars !£@%@*&^!@)(&!£\\\":<>?\",\n" +
            "            \"addr\": null,\n" +
            "            \"created_device_name\": \"javascript_web\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"guid\": \"" + GUID + "\"\n" +
            "}";
}

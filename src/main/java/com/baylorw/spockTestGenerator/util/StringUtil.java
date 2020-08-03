package com.baylorw.spockTestGenerator.util;

public class StringUtil
{     
    public static String camelToPascalCase(String token)
    {
        String newValue = token.substring(0,1).toUpperCase() + 
                          token.substring(1, token.length());
        
        return newValue;
    }

    public static String camelToSnakeCase(String token)
    {
        //--- It's basically the same process.
        return pascalToSnakeCase(token);
    }

    static public String escapeForRegularExpression(String token)
    {
        //--- Escape the characters we know are used in tags and are reserved words in regex.
        token = token.replace("$", "\\$");
        token = token.replace("[", "\\[");
        token = token.replace("]", "\\]");
        return token;
    }

    static public String pascalToCamelCase(String token)
    {
        String newValue = token.substring(0,1).toLowerCase() + 
                          token.substring(1, token.length());
        
        return newValue;
    }    

    static public String pascalToSnakeCase(String token)
    {
        StringBuilder sb = new StringBuilder();
        
        char[] letters = token.toCharArray();
        
        //--- First letter is lowercase
        sb.append(Character.toLowerCase(letters[0]));

        //--- Convert numbers like 123 to _123 (e.g. abc123def = abc_123_def).
        boolean isInANumericBlock = false;

        //--- For the rest, replace Upper with _lower
        for (int i = 1; i < letters.length; i++) 
        {
            char letter = letters[i];

            //--- Found an upper case letter
            if (Character.isUpperCase(letter))
            {
                sb.append('_');
                letter = Character.toLowerCase(letter);
                isInANumericBlock = false;
            }
            //--- Switched from letters to numbers
            if (!isInANumericBlock && Character.isDigit(letter))
            {
                isInANumericBlock = true;
                sb.append('_');
            }
            //--- Switched from numbers to letters
            else if (isInANumericBlock && !Character.isDigit(letter))
            {
                isInANumericBlock = false;
                sb.append('_');
            }
            sb.append(letter);
        }

        return sb.toString();
    }

    static public int toInt(String stringValue, int defaultValue)
    {
        int numericValue = defaultValue;
        try
        {
            stringValue = stringValue.trim();
            numericValue = Integer.parseInt(stringValue);
        }
        catch (Exception ex) {}
        return numericValue;
    }
}

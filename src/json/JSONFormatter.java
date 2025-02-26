package json;

public class JSONFormatter {
    
    public static String formatJson(String json) {
        // Ensure keys are quoted
        json = quoteKeys(json);
        
        StringBuilder formattedJson = new StringBuilder();
        int indentLevel = 0;
        boolean inQuotes = false;

        for (int i = 0; i < json.length(); i++) {
            char currentChar = json.charAt(i);

            // Handle quotes to avoid formatting inside strings
            if (currentChar == '\"') {
                inQuotes = !inQuotes;
            }

            // If it's a structural character (brackets or commas), format it
            if (!inQuotes && (currentChar == '{' || currentChar == '[')) {
                formattedJson.append(currentChar);
                formattedJson.append("\n");
                indentLevel++;
                addIndentation(formattedJson, indentLevel);
            } else if (!inQuotes && (currentChar == '}' || currentChar == ']')) {
                formattedJson.append("\n");
                indentLevel--;
                addIndentation(formattedJson, indentLevel);
                formattedJson.append(currentChar);
            } else if (!inQuotes && currentChar == ',') {
                formattedJson.append(currentChar);
                formattedJson.append("\n");
                addIndentation(formattedJson, indentLevel);
            } else {
                formattedJson.append(currentChar);
            }
        }

        return formattedJson.toString();
    }

    // Helper method to add indentation
    private static void addIndentation(StringBuilder formattedJson, int indentLevel) {
        for (int i = 0; i < indentLevel; i++) {
            formattedJson.append("    ");  // Use 4 spaces for indentation
        }
    }

    // Ensure that keys are wrapped in double quotes
    private static String quoteKeys(String json) {
        // Replace unquoted keys with quoted keys
        return json.replaceAll("([,{])([^\"{,}]*)(?=:)", "$1\"$2\"");
    }
}

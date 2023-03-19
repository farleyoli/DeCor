package fso.decor;

public class Card {
    private final String front;
    private final String back;
    private final int beginningPage;
    private final double beginningPercentage;
    private final int endPage;
    private final double endPercentage;
    private final int id;
    private final String hash;
    public Card (String front, String back, int beginningPage, double beginningPercentage, int endPage,
                           double endPercentage, int id, String hash) {
        assert hash != null;
        this.front = front;
        this.back = back;
        this.beginningPage = beginningPage;
        this.beginningPercentage = beginningPercentage;
        this.endPage = endPage;
        this.endPercentage = endPercentage;
        this.id = id;
        this.hash = hash;
    }

    public int getBeginningPage() {
        return beginningPage;
    }

    public double getBeginningPercentage() {
        return beginningPercentage;
    }

    public int getEndPage() {
        return endPage;
    }

    public double getEndPercentage() {
        return endPercentage;
    }

    public String toString() {
        return "Front: " + front + "\n" + "Back: " + back + "\n";
    }

    public String getFront() {
        return front;
    }

    public String getBack() {
        return back;
    }

    public int getId() {
        return id;
    }

    public boolean intersects(int page, double position) {
        boolean isAboveBeginning = (page == getBeginningPage() && position >= getBeginningPercentage())
                || page > getBeginningPage();
        boolean isBelowEnd = (page == getEndPage() && position <= getEndPercentage())
                || page < getEndPage();
        return isAboveBeginning && isBelowEnd;
    }

    public String getSerialiseString(String delimiter) {
        // front, back, beginningPage, beginningPercentage, endPage, endPercentage, id
        StringBuilder ret = new StringBuilder();
        ret.append(front);
        ret.append(delimiter);
        ret.append(back);
        ret.append(delimiter);
        ret.append(beginningPage);
        ret.append(delimiter);
        ret.append(beginningPercentage);
        ret.append(delimiter);
        ret.append(endPage);
        ret.append(delimiter);
        ret.append(endPercentage);
        ret.append(delimiter);
        ret.append(id);
        return ret.toString();
    }

    public String getAnkiRequest() {
        GlobalConfig config = GlobalConfig.getInstance(hash);
        return String.format("{\n" +
                "    \"action\": \"addNote\",\n" +
                "    \"version\": 6,\n" +
                "    \"params\": {\n" +
                "        \"note\": {\n" +
                "            \"deckName\": \"%s\",\n" +
                "            \"modelName\": \"DeCor\",\n" +
                "            \"fields\": {\n" +
                "                \"front\": \"%s\",\n" +
                "                \"back\": \"%s\",\n" +
                "                \"beginningPage\": \"%s\",\n" +
                "                \"beginningPercentage\": \"%s\",\n" +
                "                \"endPage\": \"%s\",\n" +
                "                \"endPercentage\": \"%s\",\n" +
                "                \"id\": \"%s\",\n" +
                "                \"cardKey\": \"%s\"\n" +
                "            },\n" +
                "            \"options\": {\n" +
                "                \"allowDuplicate\": false,\n" +
                "                \"duplicateScope\": \"deck\",\n" +
                "                \"duplicateScopeOptions\": {\n" +
                "                    \"deckName\": \"%s\",\n" +
                "                    \"checkChildren\": false,\n" +
                "                    \"checkAllModels\": false\n" +
                "                }\n" +
                "            },\n" +
                "            \"tags\": [\n" +
                "                \"DeCor\"\n" +
                "            ]\n" +
                "        }\n" +
                "    }\n" +
                "}", "DeCor::" + config.getPdfName(), front, back, beginningPage, beginningPercentage, endPage, endPercentage,
                id, hash + "_" + id, "DeCor::" + config.getPdfName());
    }
}

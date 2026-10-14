public class Calculator {
    private int[] atBats;

    public void setAtBats(int size) {
        this.atBats = new int[size];
    }

    public void setAtBatResult(int index, int result) {
        atBats[index] = result;
    }

    public double calculateAverage() {
        double hits = 0;
        for (int result : atBats) {
            if (result > 0) hits++;
        }
        return hits / atBats.length;
    }

    public double calculateSlugging() {
        double totalBases = 0;
        for (int result : atBats) {
            totalBases += result;
        }
        return totalBases / atBats.length;
    }
}

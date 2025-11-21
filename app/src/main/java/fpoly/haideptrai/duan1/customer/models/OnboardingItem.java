package fpoly.haideptrai.duan1.customer.models;

public class OnboardingItem {
    private final int imageResId;
    private final int titleResId;
    private final int descriptionResId;

    public OnboardingItem(int imageResId, int titleResId, int descriptionResId) {
        this.imageResId = imageResId;
        this.titleResId = titleResId;
        this.descriptionResId = descriptionResId;
    }

    public int getImageResId() {
        return imageResId;
    }

    public int getTitleResId() {
        return titleResId;
    }

    public int getDescriptionResId() {
        return descriptionResId;
    }
}


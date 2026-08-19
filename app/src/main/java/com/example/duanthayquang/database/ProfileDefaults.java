package com.example.duanthayquang.database;

public final class ProfileDefaults {

    public static final String LEVEL_JUNIOR = "junior";
    public static final String LEVEL_HIGH = "high";
    public static final String LEVEL_UNI = "uni";

    public static final String STYLE_SHORT = "short";
    public static final String STYLE_STEPWISE = "stepwise";
    public static final String STYLE_DETAILED = "detailed";

    public static final String SUBJECT_MATH = "math";
    public static final String SUBJECT_SCIENCE = "science";
    public static final String SUBJECT_CODE = "code";
    public static final String SUBJECT_HISTORY = "history";
    public static final String SUBJECT_LANGUAGE = "language";
    public static final String SUBJECT_OTHER = "other";

    private ProfileDefaults() {
    }

    public static Profile createDefault(long userId) {
        Profile profile = new Profile();
        profile.userId = userId;
        profile.onboarded = false;
        profile.level = LEVEL_HIGH;
        profile.subjects = SUBJECT_MATH + "," + SUBJECT_SCIENCE;
        profile.style = STYLE_STEPWISE;
        profile.xp = 0;
        profile.streak = 0;
        return profile;
    }

    public static void ensureExists(ProfileDao profileDao, long userId) {
        if (profileDao.getByUserId(userId) == null) {
            profileDao.insert(createDefault(userId));
        }
    }
}
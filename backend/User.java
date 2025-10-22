import java.util.ArrayList;

public class User{

    private String name;

    private String id;

    private String availability;

    private ArrayList<Skill> skills;

    private int totalXP;

    public User(){
        name = "DEFAULT";
        id = "-1";
        availability = "Never";
        skills = new ArrayList<Skill>();
        totalXP = 0;
    }

    public User(String name, String id, String availability, ArrayList<Skill> skills, int xp){
        this.name = name;
        this.id = id;
        this.availability = availability;
        this.skills = skills;
        this.totalXP = xp;
    }

    public void addSkill(Skill skill)
    {
        if (skills.contains(skill))
        {
            return; //user already has that skill
        }
        skills.add(skill);
        totalXP += skill.getXP();
    }
    public String getName(){
        return name;
    }
    public ArrayList<Skill>() getAllSkills(){
        return skills;
    }
    

    public void removeSkill(Skill skill)
    {
        if (skills.contains(skill))
        {
            //reduce the total xp that the user has by the amount of xp in the skill
            totalXP -= skills.get(skills.indexOf(skill)).getXP();

            skills.remove(skill);
            //in case we want to add error handling later
            return;
        }
    }


    public void addXP(int xp, Skill skill)
    {
        //we may need to rethink how we store a user's skills as this seems suboptimal
        skills.get(skills.indexOf(skill)).addXP(xp);
        totalXP += xp;
    }

    public int getSkillXP(Skill skill)
    {
        return skills.get(skills.indexOf(skill)).getXP();
    }

    public float matchSkills(ArrayList<Skill> requiredSkills)
    {
        if (requiredSkills.isEmpty())
            return -1.0f;

        float matchLevels = 0.0f;
        float averageWantedLevel = 0.0f;

        //this is kinda ineffecient and will likely need to be optimized later
        for (Skill skill : requiredSkills)
        {
            if (skills.contains(skill))
            {
                matchLevels += skills.get(skills.indexOf(skill)).getLevel();
            }
            averageWantedLevel += skill.getLevel();
        }

        averageWantedLevel = averageWantedLevel/requiredSkills.size();

        return (matchLevels / averageWantedLevel) * 100.0f;
    }
}
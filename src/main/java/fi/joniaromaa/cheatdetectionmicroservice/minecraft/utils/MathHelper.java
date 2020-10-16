package fi.joniaromaa.cheatdetectionmicroservice.minecraft.utils;

//From MC
public class MathHelper
{
    private static final float[] SIN_TABLE = new float[65536];

    static
    {
        for (int i = 0; i < 65536; ++i)
        {
            SIN_TABLE[i] = (float)Math.sin((double)i * Math.PI * 2.0D / 65536.0D);
        }
    }
    
    public static float sin(float p_76126_0_)
    {
        return SIN_TABLE[(int)(p_76126_0_ * 10430.378F) & 65535];
    }
    
    public static float cos(float value)
    {
        return SIN_TABLE[(int)(value * 10430.378F + 16384.0F) & 65535];
    }
    
    public static float sqrt_float(float value)
    {
        return (float)Math.sqrt((double)value);
    }
    
    public static double clamp_double(double num, double min, double max)
    {
        return num < min ? min : (num > max ? max : num);
    }
}

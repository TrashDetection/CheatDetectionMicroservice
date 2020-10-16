package net.minecraftforge.fml.relauncher;

import java.security.Permission;

public class FMLSecurityManager extends SecurityManager
{
    @Override
    public void checkPermission(Permission perm)
    {
        String permName = perm.getName() != null ? perm.getName() : "missing";
        if (permName.startsWith("exitVM"))
        {
            Class<?>[] classContexts = getClassContext();
            String callingClass = classContexts.length > 4 ? classContexts[4].getName() : "none";
            String callingParent = classContexts.length > 5 ? classContexts[5].getName() : "none";
            // FML is allowed to call system exit and the Minecraft applet (from the quit button)
            if (!(callingClass.startsWith("net.minecraftforge.fml.")
                    || "net.minecraft.server.dedicated.ServerHangWatchdog$1".equals(callingClass)
                    || "net.minecraft.server.dedicated.ServerHangWatchdog".equals(callingClass)
                    || ( "net.minecraft.client.Minecraft".equals(callingClass) && "net.minecraft.client.Minecraft".equals(callingParent))
                    || ("net.minecraft.server.dedicated.DedicatedServer".equals(callingClass) && "net.minecraft.server.MinecraftServer".equals(callingParent)))
                    )
            {
                throw new RuntimeException("Forge says no no to exit");
            }
        }
    }

    @Override
    public void checkPermission(Permission perm, Object context)
    {
        this.checkPermission(perm);
    }
}

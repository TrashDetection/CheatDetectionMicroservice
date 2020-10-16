package net.minecraftforge.fml.relauncher;

import java.security.Permission;

public class FMLSecurityManager extends SecurityManager
{
    @Override
    public void checkPermission(Permission perm)
    {
    	String permName = perm.getName();
    	if (permName == null)
    	{
    		return;
    	}

    	if (permName.startsWith("exitVM"))
        {
            throw new RuntimeException("Don't quit the VM by force");
        }
    }

    @Override
    public void checkPermission(Permission perm, Object context)
    {
        this.checkPermission(perm);
    }
}

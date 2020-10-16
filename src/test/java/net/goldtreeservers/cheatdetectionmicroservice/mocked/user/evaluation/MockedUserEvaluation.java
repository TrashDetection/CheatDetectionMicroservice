package net.goldtreeservers.cheatdetectionmicroservice.mocked.user.evaluation;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.goldtreeservers.cheatdetectionmicroservice.user.BaseUser;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.Module;

public class MockedUserEvaluation extends UserEvaluation
{
	private Set<Class<? extends Module>> modulesToAdd;
	
	private Map<Class<? extends Module>, Module> modulesByClass;
	
	public MockedUserEvaluation(BaseUser user) throws Exception
	{
		super(user);
		
		this.modulesToAdd = new HashSet<>(UserEvaluation.DEFAULT_MODULES);
		
		this.modulesByClass = new IdentityHashMap<>();
	}

	public void addModule(Class<? extends Module> module)
	{
		this.modulesToAdd.add(module);
	}
	
	@Override
	protected List<Module> generateModules() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
	{
		List<Module> generatedModules = super.generateModules(this.modulesToAdd);
		
		for(Module module : generatedModules)
		{
			this.modulesByClass.put(module.getClass(), module);
		}
		
		return generatedModules;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Module> T getModule(Class<? extends T> clazz)
	{
		return (T)this.modulesByClass.get(clazz);
	}
	
	@Override
	protected void onException(Throwable e)
	{
		throw new RuntimeException(e);
	}
	
	@Override
	public void close()
	{
		
	}
}

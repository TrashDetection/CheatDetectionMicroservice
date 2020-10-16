package fi.joniaromaa.cheatdetectionmicroservice.services.mixin;

/*public class MixinServiceCheatDetection extends MixinServiceAbstract
{
    private IClassBytecodeProvider bytecodeProvider;
    private IClassProvider classProvider;
    
    private IClassProcessor transformationHandler;
    
	private ContainerHandleCheatDetection rootContainer = new ContainerHandleCheatDetection();
	
    private IConsumer<Phase> phaseConsumer;

    public void onStartup()
    {
        this.phaseConsumer.accept(Phase.DEFAULT);
    }
    
    @SuppressWarnings("deprecation")
	@Override
    public void wire(Phase phase, IConsumer<Phase> phaseConsumer)
    {
        super.wire(phase, phaseConsumer);
        
        this.phaseConsumer = phaseConsumer;
    }
    
	@Override
	public String getName()
	{
		return "CheatDetection";
	}

    @Override
    public CompatibilityLevel getMinCompatibilityLevel()
    {
        return CompatibilityLevel.JAVA_8;
    }
    
	@Override
	public boolean isValid()
	{
		return true;
	}

	@Override
	public IClassProvider getClassProvider()
	{
		if (this.classProvider == null)
		{
			this.classProvider = new CheatDetectionClassProvider();
		}
		
		return this.classProvider;
	}

	@Override
	public IClassBytecodeProvider getBytecodeProvider()
	{
        //if (this.bytecodeProvider == null)
        //{
        //	this.bytecodeProvider = new CheatDetectionBytecodeProvider();
        //}
        
        return this.bytecodeProvider;
	}

	@Override
	public ITransformerProvider getTransformerProvider()
	{
		System.out.println("get transformer");
		
		return null;
	}

	@Override
	public IClassTracker getClassTracker()
	{
		return null; //TODO: MAYBE?
	}

	@Override
	public IMixinAuditTrail getAuditTrail()
	{
		return null; //TODO: MAYBE?
	}
	
	public IClassProcessor getTransformationHandler()
	{
        if (this.transformationHandler == null)
        {
            this.transformationHandler = new MixinTransformationHandler();
        }
        
        return this.transformationHandler;
    }

	@Override
	public Collection<String> getPlatformAgents()
	{
		return ImmutableList.of(
            "fi.joniaromaa.cheatdetectionmicroservice.services.mixin.platform.MixinPlatformAgentCheatDetection"
        );
	}

	@Override
	public IContainerHandle getPrimaryContainer()
	{
		return this.rootContainer;
	}

	@Override
	public InputStream getResourceAsStream(String name)
	{
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
	}
}
*/
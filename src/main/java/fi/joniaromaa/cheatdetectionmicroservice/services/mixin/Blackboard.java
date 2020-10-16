package fi.joniaromaa.cheatdetectionmicroservice.services.mixin;

/*public class Blackboard implements IGlobalPropertyService
{
    private class Key implements IPropertyKey
    {
        private final String key;

        Key(String key)
        {
            this.key = key;
        }
        
        @Override
        public String toString()
        {
            return this.key;
        }
    }

    private final Map<IPropertyKey, Object> values = new HashMap<IPropertyKey, Object>(); 
    
    @Override
    public IPropertyKey resolveKey(String name)
    {
    	return new Key(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <T> T getProperty(IPropertyKey key)
    {
    	return (T)this.values.get(key);
    }

    @Override
    public final void setProperty(IPropertyKey key, Object value)
    {
    	this.values.put(key, value);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public final <T> T getProperty(IPropertyKey key, T defaultValue)
    {
        return (T)this.values.getOrDefault(key, defaultValue);
    }
    
    @Override
    public final String getPropertyString(IPropertyKey key, String defaultValue)
    {
    	return (String)this.values.getOrDefault(key, defaultValue);
    }
}*/
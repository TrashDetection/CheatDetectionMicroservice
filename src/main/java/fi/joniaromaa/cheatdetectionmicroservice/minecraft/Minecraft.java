package fi.joniaromaa.cheatdetectionmicroservice.minecraft;

import fi.joniaromaa.cheatdetectionmicroservice.service.loader.ServiceURLClassLoader;
import fi.joniaromaa.cheatdetectionmicroservice.service.loader.ServiceWhitelistURLClassLoader;
import lombok.Getter;

public class Minecraft
{
    @Getter private static ServiceURLClassLoader minecraftHookClassLoader = ServiceWhitelistURLClassLoader.build((l) ->
    {
        l.addIncludedPackage("fi.joniaromaa.cheatdetectionmicroservice.service.minecraft.");
        l.addIncludedPackage("fi.joniaromaa.cheatdetectionmicroservice.services.minecraft.");

        l.addIncludedPackage("fi.joniaromaa.minecrafthook.common.");

        l.addIncludedResources("META-INF/services/fi.joniaromaa.cheatdetectionmicroservice.service.minecraft.IMinecraftHookService");
        l.addIncludedResources("fi/joniaromaa/minecrafthook/common/");
    });
}

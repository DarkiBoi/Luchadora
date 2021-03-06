package cookiedragon.luchadora.module;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import cookiedragon.luchadora.event.api.EventDispatcher;
import cookiedragon.luchadora.event.luchadora.ModuleInitialisationEvent;
import cookiedragon.luchadora.module.impl.combat.*;
import cookiedragon.luchadora.module.impl.dev.*;
import cookiedragon.luchadora.module.impl.movement.*;
import cookiedragon.luchadora.module.impl.player.*;
import cookiedragon.luchadora.module.impl.render.*;
import cookiedragon.luchadora.module.impl.ui.AbstractHudElement;
import cookiedragon.luchadora.module.impl.ui.elements.SearchBarElement;
import cookiedragon.luchadora.module.impl.ui.elements.clickgui.CategoryElement;
import cookiedragon.luchadora.module.impl.ui.elements.clickgui.GuiModule;
import cookiedragon.luchadora.module.impl.world.LiquidInteractModule;
import cookiedragon.luchadora.util.SimpleClassLoader;
import cookiedragon.luchadora.value.Value;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author cookiedragon234 15/Dec/2019
 */
public class ModuleManager
{
	private static final Map<AbstractModule, List<Value>> modules = new HashMap<>();
	
	public static void init()
	{
		EventDispatcher.dispatch(new ModuleInitialisationEvent.Pre());
		
		/*new SimpleClassLoader<AbstractModule>()
			.build(
				BreakHighlightModule.class,
				CrystalAuraModule.class,
				
				InvalidTeleportModule.class,
				
				NoSlowModule.class,
				
				BreakTweaksModule.class,
				MultiTaskModule.class,
				NoEntityTraceModule.class,
				ReachModule.class,
				RotationLockModule.class,
				
				EspModule.class,
				ExtraRenderModule.class,
				
				LiquidInteractModule.class,
				
				GuiModule.class,
				SearchBarElement.class
			)
			.initialise(
				module -> modules.put(module, module.getValues()),
				module -> {},
				(module, e) -> new RuntimeException("Failed to initialise module '" + module.getName() + "'", e)
			);*/
		
		try
		{
			for (ClassPath.ClassInfo classInfo : ClassPath.from(Launch.classLoader).getAllClasses())
			{
				if (classInfo.getName().startsWith("cookiedragon.luchadora."))
				{
					try
					{
						Class<?> aClass = classInfo.load();
						if (!Modifier.isAbstract(aClass.getModifiers()) && AbstractModule.class.isAssignableFrom(aClass))
						{
							System.out.println("Found " + classInfo.getName());
							for (Constructor<?> constructor : aClass.getConstructors())
							{
								if (constructor.getParameterCount() == 0)
								{
									AbstractModule instance = (AbstractModule)constructor.newInstance();
									modules.put(instance, instance.getValues());
									break;
								}
							}
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException("Failure loading modules", e);
		}
		
		{
			GuiModule guiModule = getModule(GuiModule.class);
			
			for (Category category : Category.class.getEnumConstants())
			{
				CategoryElement module = new CategoryElement(category, guiModule);
				modules.put(module, module.getValues());
			}
		}
		
		System.out.println(modules.toString());
		
		EventDispatcher.dispatch(new ModuleInitialisationEvent.Post());
		System.out.println("Post module init");
	}
	
	public static <T extends AbstractModule> T getModule(Class<T> moduleClazz)
	{
		for (AbstractModule module : modules.keySet())
		{
			if (moduleClazz.isAssignableFrom(module.getClass()))
				return Objects.requireNonNull(moduleClazz.cast(module));
		}
		throw new RuntimeException("AbstractModule " + moduleClazz + " was not initialised!");
	}
	
	public static Set<AbstractModule> getModules()
	{
		return modules.keySet();
	}
	
	public static Map<AbstractModule, List<Value>> getAll()
	{
		return modules;
	}
	
	public static List<Value> getValuesForModule(AbstractModule module)
	{
		return modules.get(module);
	}
	
	public static Value findValueForModule(AbstractModule module, String valueName)
	{
		for (Value value : modules.get(module))
		{
			if (value.getName().equalsIgnoreCase(valueName))
				return value;
		}
		throw new RuntimeException("Couldnt find value '" + valueName + "' for module '" + module.getName() + "'");
	}
	
	public static Iterator<AbstractModule> iterator()
	{
		return modules.keySet().iterator();
	}
}

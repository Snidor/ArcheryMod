package com.wurmonline.client.renderer.gui;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.PreInitable;
import org.gotti.wurmunlimited.modloader.interfaces.WurmClientMod;

import com.wurmonline.client.game.World;
import com.wurmonline.client.renderer.backend.Queue;
import com.wurmonline.client.renderer.cell.CreatureCellRenderable;

import javassist.ClassPool;
import javassist.CtClass;

public class ArcheryMod implements WurmClientMod, PreInitable, Initable
{
	public static Logger LOGGER = Logger.getLogger( "ArcheryMod" );
	public static HeadsUpDisplay mHud;
	public static World mWorld;
	public static TargetWindow mWindow;
	public static String mOriginalName;
	public static int mDistance;
	public static Queue mQueue;
	@Override
	public void preInit() 
	{		
		LOGGER.log( Level.INFO, "PreInit ArcheryMod" );
		try 
		{	
			ClassPool lClassPool = HookManager.getInstance().getClassPool();
			
	        HookManager.getInstance().registerHook( "com.wurmonline.client.renderer.gui.HeadsUpDisplay", "init", "(II)V", () -> ( pProxy, pMethod, pArgs ) -> 
			{
				pMethod.invoke( pProxy, pArgs );
				mHud = ( HeadsUpDisplay ) pProxy;
				return null;
			});

	        HookManager.getInstance().registerHook( "com.wurmonline.client.renderer.WorldRender", "renderPickedItem", "(Lcom/wurmonline/client/renderer/backend/Queue;)V", () -> ( pProxy, pMethod, pArgs ) -> 
			{
				pMethod.invoke(pProxy, pArgs);
					Class<?> lCls = pProxy.getClass();
	
				mWorld = ReflectionUtil.getPrivateField( pProxy, ReflectionUtil.getField( lCls, "world" ) );
	
				return null;
			});
			
			HookManager.getInstance().registerHook( "com.wurmonline.client.renderer.gui.TargetIronRenderer", "renderComponent", "(Lcom/wurmonline/client/renderer/backend/Queue;F)V", () -> ( pProxy, pMethod, pArgs ) -> 
			{
				pMethod.invoke(pProxy, pArgs);
				Class<?> lCls = pProxy.getClass();
				
				mWindow = ReflectionUtil.getPrivateField( pProxy, ReflectionUtil.getField( lCls, "targetWindow" ) );
				
				return null;
			});			

			CtClass lCtTargetWindow = lClassPool.getCtClass( "com.wurmonline.client.renderer.gui.TargetWindow" );
			lCtTargetWindow.getMethod( "setTarget", "(JLjava/lang/String;Lcom/wurmonline/client/renderer/cell/CreatureCellRenderable;)V" ).insertBefore( "com.wurmonline.client.renderer.gui.ArcheryMod.saveOriginalName($3);" );
		} 
		catch ( Throwable e ) 
		{
			LOGGER.log( Level.SEVERE, "Error ArcheryMod", e.getMessage() );
		}	
	}

	@Override
	public void init() 
	{
		LOGGER.log( Level.INFO, "Init ArcheryMod" );
		try 
		{
			ClassPool lClassPool = HookManager.getInstance().getClassPool();
			lClassPool.get( "com.wurmonline.client.renderer.gui.TargetIronRenderer" ).getMethod( "renderComponent","(Lcom/wurmonline/client/renderer/backend/Queue;F)V" ).insertAfter( "com.wurmonline.client.renderer.gui.ArcheryMod.setTextColor($1);" );			
		}
		catch ( Throwable e ) 
		{
			LOGGER.log( Level.SEVERE, "Error ArcheryMod", e.getMessage() );
		}
	}

	public static String calcDistance()
	{
		float lXDis = mWorld.getPlayerPosX() - mWindow.creature.getXPos();
		float lYDis = mWorld.getPlayerPosY() - mWindow.creature.getYPos();
		
		if ( lXDis < 0 )
		{
			lXDis *= ( -1 );
		}
		
		if ( lYDis < 0 )
		{
			lYDis *= ( -1 );
		}
		
		mDistance = (int)Math.sqrt( ( lXDis * lXDis ) + ( lYDis * lYDis ) );
		
		String lReturn = "";
		lReturn += mDistance;
		lReturn += "m  ";
		lReturn += mOriginalName;
		return lReturn;
	}
	
	public static void saveOriginalName( CreatureCellRenderable pCreature )
	{
		if ( mWindow.creature != null )
		{
			String lOrgName = "";
			if ( Character.isDigit( mWindow.creature.getCreatureData().getName().charAt( 0 ) ) )
			{
				lOrgName = mWindow.creature.getCreatureData().getName().substring( mWindow.creature.getCreatureData().getName().indexOf(" ") + 2 );
			}
			else
			{
				lOrgName = mWindow.creature.getCreatureData().getName();			
			}
			mWindow.creature.getCreatureData().renameNameOnly( lOrgName );			
		}
		
		if ( pCreature != null )
		{			
			if ( Character.isDigit( pCreature.getHoverName().charAt( 0 ) ) )
			{
				mOriginalName = pCreature.getHoverName().substring( pCreature.getHoverName().indexOf(" ") + 1 );
			}
			else
			{
				mOriginalName = pCreature.getHoverName();			
			}
		}
	}
	
	public static void setRed( Queue pQueue )
	{
		mWindow.text.paint( pQueue, mWindow.creature.getCreatureData().getName(), 1.0f, 0.0f, 0.0f, 1.0f );
	}
	
	public static void setYellow( Queue pQueue )
	{
		mWindow.text.paint( pQueue, mWindow.creature.getCreatureData().getName(), 1.0f, 1.0f, 0.0f, 1.0f );
	}
	
	public static void setGreen( Queue pQueue )
	{
		mWindow.text.paint( pQueue, mWindow.creature.getCreatureData().getName(), 0.0f, 1.0f, 0.0f, 1.0f );
	}
	
	public static void setPurple( Queue pQueue )
	{
		mWindow.text.paint( pQueue, mWindow.creature.getCreatureData().getName(), 0.95f, 0.0f, 0.95f, 1.0f );
	}
	
	public static void setWhite( Queue pQueue )
	{
		mWindow.text.paint( pQueue, mWindow.creature.getCreatureData().getName(), 1.0f, 1.0f, 1.0f, 1.0f );
	}
	
	public static void setTextColor( Queue pQueue )
	{
		if ( mWindow.creature != null )
		{
			mWindow.creature.getCreatureData().renameNameOnly( calcDistance() );
			if ( mWorld.getPlayer().getPlayerBody().getEquipment( 1 ) != null )
			{
				if ( mWorld.getPlayer().getPlayerBody().getEquipment( 1 ).getItemName().contains( "model.bow.long" ) )
				{	
					if ( mDistance == 80 )
					{
						setGreen( pQueue );
					}
					else if ( ( mDistance < 40 ) || ( mDistance > 180) )
					{
						setRed( pQueue );
					}
					else
					{
						setYellow( pQueue );
					}
				}
				else if ( mWorld.getPlayer().getPlayerBody().getEquipment( 1 ).getItemName().contains( "model.bow.medium" ) )
				{
					if ( mDistance == 40 )
					{
						setGreen( pQueue );
					}
					else if ( ( mDistance < 20 ) || ( mDistance > 180) )
					{
						setRed( pQueue );
					}
					else
					{
						setYellow( pQueue );
					}
				}
				else if ( mWorld.getPlayer().getPlayerBody().getEquipment( 1 ).getItemName().contains( "model.bow.short" ) )
				{
					if ( mDistance == 20 )
					{
						setGreen( pQueue );
					}
					else if ( ( mDistance < 4 ) || ( mDistance > 180) )
					{
						setRed( pQueue );
					}
					else
					{
						setYellow( pQueue );
					}
				}
				else
				{
					setWhite( pQueue );
				}
			}
		}
	}
}

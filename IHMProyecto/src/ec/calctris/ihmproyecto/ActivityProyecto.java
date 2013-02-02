package ec.calctris.ihmproyecto;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.AutoParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import android.content.Intent;
import android.hardware.SensorManager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class ActivityProyecto extends SimpleBaseGameActivity implements IAccelerationListener, IOnSceneTouchListener{

	//Constantes
	private static final int CAMERA_WIDTH = 480; //Ancho 480px
    private static final int CAMERA_HEIGHT = 800; //Alto 800px
    public int tamarreglo = 8;

    //Variables
    private BitmapTextureAtlas mFondo;//Arreglo de fondo
    private ITextureRegion mFondoRegion;//Texture del fondo
    
    private BitmapTextureAtlas mNube;
    private ITextureRegion mNubeRegion;
    
    private BitmapTextureAtlas mBoton;//Arreglo de botones
    private ITextureRegion mPausa;//BotonJugar
    
    private BitmapTextureAtlas mEsferas;
    private ITextureRegion[] mFondoEsferas = new ITextureRegion[tamarreglo];
    private List<Sprite>mSpheres;
    
    private Scene mScene;
    private PhysicsWorld myPhysicsWorld;
    
    //Parte l�gica
    public int Matriz[][] = new int[6][16];
    
    // ============================================================
    // Method: onCreateEmgineOptions
    // ============================================================
    @Override
    public EngineOptions onCreateEngineOptions() {
        
    	final Camera mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
        return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);        
    }

    // ============================================================
    // Method: onCreateResources
    // ============================================================
    @Override
    public void onCreateResources() {
    	
    	//Obteniendo la carpeta donde estaran las imagenes
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
        
        //Para el fondo
        this.mFondo = new BitmapTextureAtlas(this.getTextureManager(), 480, 800, TextureOptions.BILINEAR);//Arreglo donde almaceno la imagen
        this.mFondoRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mFondo, this, "PantallaGame.png", 0, 0);
        this.mFondo.load();//Cargo la imagen
        
        //Para el fondo con la nube en movimiento
        this.mNube = new BitmapTextureAtlas(this.getTextureManager(), 227, 85, TextureOptions.BILINEAR);
        this.mNubeRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mNube, this, "Nubes_pequenas.png", 0, 0);
        this.mNube.load();
        
        //Para los botones
        this.mBoton = new BitmapTextureAtlas(this.getTextureManager(),50, 50, TextureOptions.BILINEAR);//Arreglo para los botones iniciales
        this.mPausa = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBoton, this, "BotonPausa.png", 0, 0);
        this.mBoton.load();
        
        //Para las esferas
		this.mEsferas = new BitmapTextureAtlas(this.getTextureManager(), 50, 450, TextureOptions.BILINEAR);//450
		String ruta[] = {"Esfera1.png", "Esfera2.png", "Esfera3.png", "Esfera4.png", "Esfera5.png", "Esfera6.png", "Esfera7.png", "Esfera8.png", "Esfera9.png"};
		for(int i = 0; i < tamarreglo; i++){
			this.mFondoEsferas[i] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mEsferas, this, ruta[i], 0, i*50);
		}
		this.mEsferas.load();
	}

    // ============================================================
    // Method: onCreateScene
    // ============================================================
    @Override
    public Scene onCreateScene() {
    	
    	this.mEngine.registerUpdateHandler(new FPSLogger());
        this.mScene = new Scene();
        
        final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
        this.myPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
        this.mScene.setOnSceneTouchListener(this);
             
        //Para el fondo
        final AutoParallaxBackground fondo = new AutoParallaxBackground(0, 0, 0, 5);
        fondo.attachParallaxEntity(new ParallaxEntity(0.0f, new Sprite(0,0, this.mFondoRegion, vertexBufferObjectManager)));
        fondo.attachParallaxEntity(new ParallaxEntity(-10.0f, new Sprite(0, 0, this.mNubeRegion, vertexBufferObjectManager)));
        mScene.setBackground(fondo);
        
        //Para los botones
        //BotonJugar
        final Sprite boton1 = new Sprite(370, 400, this.mPausa, vertexBufferObjectManager){
        	@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY){
        		Intent intent = new Intent (ActivityProyecto.this, PantallaPausa.class);
        		startActivity(intent);
        		return true;
        	}
        	
        };
        mScene.registerTouchArea(boton1);//Se registra el evento
        mScene.attachChild(boton1);//Se lo agrega a la escena

        //El mundo f�sico
        final Rectangle pared1 = new Rectangle(0, 0, 1, CAMERA_HEIGHT, vertexBufferObjectManager);
        final Rectangle pared2 = new Rectangle(0, CAMERA_HEIGHT, 317, 1, vertexBufferObjectManager);
        final Rectangle pared3 = new Rectangle(317, 0, 1, CAMERA_HEIGHT, vertexBufferObjectManager);
        final Rectangle pared4 = new Rectangle(0, 0, CAMERA_WIDTH, 1, vertexBufferObjectManager);
        
        //Le doy texture dentro del mundo fisico
        final FixtureDef texturepared = PhysicsFactory.createFixtureDef(0, 0f, 10.0f);
        PhysicsFactory.createBoxBody(this.myPhysicsWorld,pared1,BodyType.StaticBody,texturepared);
        PhysicsFactory.createBoxBody(this.myPhysicsWorld,pared2,BodyType.StaticBody,texturepared);
        PhysicsFactory.createBoxBody(this.myPhysicsWorld,pared3,BodyType.StaticBody,texturepared);
        PhysicsFactory.createBoxBody(this.myPhysicsWorld,pared4,BodyType.StaticBody,texturepared);
                    
        this.mScene.attachChild(pared1);
        this.mScene.attachChild(pared2);
        this.mScene.attachChild(pared3);
        this.mScene.attachChild(pared4);
        
        this.mScene.registerUpdateHandler(this.myPhysicsWorld);
        
        //Agregando esferas al escenario con tiempo de 5 segundos LOL
        createSpheresbyTimeHandler();
        return this.mScene; 
           
    }
    
    /* ======================================================
	 * Metodo que a�ade las esferas por un lapso de 5 segundo
	 ========================================================*/
	public void createSpheresbyTimeHandler(){
		
		TimerHandler timeSpheres;
		float mEffectSpawnDelay = 5f;
		
		timeSpheres = new TimerHandler(mEffectSpawnDelay, true, new ITimerCallback(){
			@Override
            public void onTimePassed(TimerHandler pTimerHandler) {
				addSpheres();
            }
		});
		getEngine().registerUpdateHandler(timeSpheres);
	}
	
	/* =====================================================
	 * Metodo que me crea cada esfera y las agrega al mScene
	 =======================================================*/
	private void addSpheres() {
		
		mSpheres = new ArrayList<Sprite>();//Array of Spheres
		Random number = new Random();
        int aleatorio = number.nextInt(8);
        Random px = new Random();
        final int py = 0;
		final int num = px.nextInt(300);
		final FixtureDef textureSphere = PhysicsFactory.createFixtureDef(0, 0f, 10.0f);
				
		Sprite OneSphere = new Sprite (num, py, this.mFondoEsferas[aleatorio], this.getVertexBufferObjectManager()){
        	public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
        		if(pSceneTouchEvent.isActionMove()){
        			this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2, pSceneTouchEvent.getY() - this.getHeight() / 2);
        		}
        		return true;
        	}
        };
        Body body = PhysicsFactory.createBoxBody(this.myPhysicsWorld, OneSphere, BodyType.DynamicBody, textureSphere);
        this.myPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(OneSphere, body,true,false));
        //OneSphere.setUserData(body);
        mSpheres.add(OneSphere);
        this.mScene.attachChild(OneSphere);
        this.mScene.registerTouchArea(OneSphere);
        this.mScene.setTouchAreaBindingOnActionMoveEnabled(true);
    }
	

	/* ==================================================
	 * Metodo que me inicializa la matriz con el valor -1
	 ====================================================*/
	public void InicializarMatriz(){
		
		for(int i=0; i<Matriz.length; i++){
			for(int j=0; j<Matriz[i].length; j++){
				Matriz[i][j] = -1;
			}
		}
	}
	
	/* ====================================================================================
	 * Metodo boleano que verifica si esta en el borde y si esta disponible la celda actual
	 ======================================================================================*/
	public int esta_vacia(final int x, final int y, int [][] Matriz){
		
		//Verifica si esta dentro de la matriz
		if ((y<0) || (x<0)){
			return 1;
		}
		else{
			//pregunta si la celda esta libre o no libre
			if(Matriz[x][y] == -1){
				return 1;
			}
			else{
				return -1;
			}
		}
	}  
	
	/* =====================================
	 * Metodo para el touch en cada Sprite
	 ==================================== 
	public boolean onAreaTouched( final TouchEvent pSceneTouchEvent, final ITouchArea pTouchArea,final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
		if(pSceneTouchEvent.isActionDown()) {
			
    		return true;
		}
		return false;
	}*/
	
	public void OnResumeGame(){
		super.onResumeGame();
		this.enableAccelerationSensor(this);
	}
	
	public void OnPauseGame(){
		super.onPauseGame();
		this.disableAccelerationSensor();
	}

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		if(this.myPhysicsWorld != null){
			if(pSceneTouchEvent.isActionMove()){
				for(int i =0; i<mSpheres.size(); i++){
					float box2d_x = (pSceneTouchEvent.getX()) / 32;
					final PhysicsConnector paddlePhysicsConnector = this.myPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(mSpheres.get(i));
					Body paddleBody = paddlePhysicsConnector.getBody();
					paddleBody.setTransform(new Vector2(box2d_x, paddleBody.getPosition().y), 0);
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public void onAccelerationAccuracyChanged(AccelerationData pAccelerationData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAccelerationChanged(AccelerationData pAccelerationData) {
		// TODO Auto-generated method stub
		
	}

}

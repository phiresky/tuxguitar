package org.herac.tuxguitar.gui.system.config.items;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.ToolBar;
import org.herac.tuxguitar.gui.TuxGuitar;
import org.herac.tuxguitar.gui.editors.TGPainter;
import org.herac.tuxguitar.gui.helper.SyncThread;
import org.herac.tuxguitar.gui.system.config.TGConfigEditor;
import org.herac.tuxguitar.gui.system.config.TGConfigKeys;

public class StylesOption extends Option{
	private static final int BUTTON_WIDTH = 200;
	private static final int BUTTON_HEIGHT =0;// 25;
	
	protected boolean initialized;
	protected FontData defaultFontData;
	protected FontData noteFontData;
	protected FontData timeSignatureFontData;
	protected FontData textFontData;
	protected FontData lyricFontData;
	protected FontData printerDefaultFontData;
	protected FontData printerNoteFontData;
	protected FontData printerTSFontData;
	protected FontData printerTextFontData;
	protected FontData printerLyricFontData;
	
	protected RGB scoreNoteRGB;
	protected RGB tabNoteRGB;
	protected RGB playNoteRGB;
	protected RGB linesRGB;
	
	protected Button defaultFontButton;
	protected Button noteFontButton;
	protected Button timeSignatureFontButton;
	protected Button textFontButton;
	protected Button lyricFontButton;
	
	protected Button printerDefaultFontButton;
	protected Button printerNoteFontButton;
	protected Button printerTSFontButton;
	protected Button printerTextFontButton;
	protected Button printerLyricFontButton;
	
	protected Button scoreNoteColorButton;
	protected Button tabNoteColorButton;
	protected Button playNoteColorButton;
	protected Button linesColorButton;
	
	public StylesOption(TGConfigEditor configEditor,ToolBar toolBar,final Composite parent){
		super(configEditor,toolBar,parent,TuxGuitar.getProperty("settings.config.styles"));
		this.initialized = false;
		this.defaultFontData = new FontData();
		this.noteFontData = new FontData();
		this.timeSignatureFontData = new FontData();
		this.textFontData = new FontData();
		this.lyricFontData = new FontData();
		this.printerDefaultFontData = new FontData();
		this.printerNoteFontData = new FontData();
		this.printerTSFontData = new FontData();
		this.printerTextFontData = new FontData();
		this.printerLyricFontData = new FontData();
		this.scoreNoteRGB = new RGB(0,0,0);
		this.tabNoteRGB = new RGB(0,0,0);
		this.playNoteRGB = new RGB(0,0,0);
		this.linesRGB = new RGB(0,0,0);
	}
	
	public void createOption(){
		getToolItem().setText(TuxGuitar.getProperty("settings.config.styles"));
		getToolItem().setImage(TuxGuitar.instance().getIconManager().getOptionStyle());
		getToolItem().addSelectionListener(this);
		
		//=================================================== EDITOR STYLES ===================================================//
		showLabel(getComposite(),SWT.TOP | SWT.LEFT | SWT.WRAP,SWT.BOLD,0,TuxGuitar.getProperty("settings.config.styles.general"));
		
		Composite composite = new Composite(getComposite(),SWT.NONE);
		composite.setLayout(new GridLayout(2,false));
		composite.setLayoutData(getTabbedData());
		
		showLabel(composite,SWT.FILL,SWT.CENTER,SWT.LEFT | SWT.WRAP,SWT.NORMAL,0,TuxGuitar.getProperty("settings.config.styles.font.default"));
		this.defaultFontButton = new Button(composite, SWT.PUSH);
		this.defaultFontButton.setLayoutData(makeButtonData());
		this.addFontButtonListeners(this.defaultFontButton,this.defaultFontData);
		
		showLabel(composite,SWT.FILL,SWT.CENTER,SWT.LEFT | SWT.WRAP,SWT.NORMAL,0,TuxGuitar.getProperty("settings.config.styles.font.note"));
		this.noteFontButton = new Button(composite, SWT.PUSH);
		this.noteFontButton.setLayoutData(makeButtonData());
		this.addFontButtonListeners(this.noteFontButton,this.noteFontData);
		
		showLabel(composite,SWT.FILL,SWT.CENTER,SWT.LEFT | SWT.WRAP,SWT.NORMAL,0,TuxGuitar.getProperty("settings.config.styles.font.lyric"));
		this.lyricFontButton = new Button(composite, SWT.PUSH);
		this.lyricFontButton.setLayoutData(makeButtonData());
		this.addFontButtonListeners(this.lyricFontButton,this.lyricFontData);
		
		showLabel(composite,SWT.FILL,SWT.CENTER,SWT.LEFT | SWT.WRAP,SWT.NORMAL,0,TuxGuitar.getProperty("settings.config.styles.font.text"));
		this.textFontButton = new Button(composite, SWT.PUSH);
		this.textFontButton.setLayoutData(makeButtonData());
		this.addFontButtonListeners(this.textFontButton,this.textFontData);
		
		showLabel(composite,SWT.FILL,SWT.CENTER,SWT.LEFT | SWT.WRAP,SWT.NORMAL,0,TuxGuitar.getProperty("settings.config.styles.font.time-signature"));
		this.timeSignatureFontButton = new Button(composite, SWT.PUSH);
		this.timeSignatureFontButton.setLayoutData(makeButtonData());
		this.addFontButtonListeners(this.timeSignatureFontButton,this.timeSignatureFontData);
		
		showLabel(composite,SWT.FILL,SWT.CENTER,SWT.LEFT | SWT.WRAP,SWT.NORMAL,0,TuxGuitar.getProperty("settings.config.styles.color.score-note"));
		this.scoreNoteColorButton = new Button(composite, SWT.PUSH);
		this.scoreNoteColorButton.setLayoutData(makeButtonData());
		addColorButtonListeners(this.scoreNoteColorButton, this.scoreNoteRGB);
		
		showLabel(composite,SWT.FILL,SWT.CENTER,SWT.LEFT | SWT.WRAP,SWT.NORMAL,0,TuxGuitar.getProperty("settings.config.styles.color.tab-note"));
		this.tabNoteColorButton = new Button(composite, SWT.PUSH);
		this.tabNoteColorButton.setLayoutData(makeButtonData());
		addColorButtonListeners(this.tabNoteColorButton, this.tabNoteRGB);
		
		showLabel(composite,SWT.FILL,SWT.CENTER,SWT.LEFT | SWT.WRAP,SWT.NORMAL,0,TuxGuitar.getProperty("settings.config.styles.color.play-note"));
		this.playNoteColorButton = new Button(composite, SWT.PUSH);
		this.playNoteColorButton.setLayoutData(makeButtonData());
		addColorButtonListeners(this.playNoteColorButton, this.playNoteRGB);
		
		showLabel(composite,SWT.FILL,SWT.CENTER,SWT.LEFT | SWT.WRAP,SWT.NORMAL,0,TuxGuitar.getProperty("settings.config.styles.color.lines"));
		this.linesColorButton = new Button(composite, SWT.PUSH);
		this.linesColorButton.setLayoutData(makeButtonData());
		addColorButtonListeners(this.linesColorButton, this.linesRGB);
		
		//=================================================== PRINTER STYLES ===================================================//
		showLabel(getComposite(),SWT.TOP | SWT.LEFT | SWT.WRAP,SWT.BOLD,0,TuxGuitar.getProperty("settings.config.styles.printer"));
		
		composite = new Composite(getComposite(),SWT.NONE);
		composite.setLayout(new GridLayout(2,false));
		composite.setLayoutData(getTabbedData());
		
		showLabel(composite,SWT.FILL,SWT.CENTER,SWT.LEFT | SWT.WRAP,SWT.NORMAL,0,TuxGuitar.getProperty("settings.config.styles.font.default"));
		this.printerDefaultFontButton = new Button(composite, SWT.PUSH);
		this.printerDefaultFontButton.setLayoutData(makeButtonData());
		this.addFontButtonListeners(this.printerDefaultFontButton,this.printerDefaultFontData);
		
		showLabel(composite,SWT.FILL,SWT.CENTER,SWT.LEFT | SWT.WRAP,SWT.NORMAL,0,TuxGuitar.getProperty("settings.config.styles.font.note"));
		this.printerNoteFontButton = new Button(composite, SWT.PUSH);
		this.printerNoteFontButton.setLayoutData(makeButtonData());
		this.addFontButtonListeners(this.printerNoteFontButton,this.printerNoteFontData);
		
		showLabel(composite,SWT.FILL,SWT.CENTER,SWT.LEFT | SWT.WRAP,SWT.NORMAL,0,TuxGuitar.getProperty("settings.config.styles.font.lyric"));
		this.printerLyricFontButton = new Button(composite, SWT.PUSH);
		this.printerLyricFontButton.setLayoutData(makeButtonData());
		this.addFontButtonListeners(this.printerLyricFontButton,this.printerLyricFontData);
		
		showLabel(composite,SWT.FILL,SWT.CENTER,SWT.LEFT | SWT.WRAP,SWT.NORMAL,0,TuxGuitar.getProperty("settings.config.styles.font.text"));
		this.printerTextFontButton = new Button(composite, SWT.PUSH);
		this.printerTextFontButton.setLayoutData(makeButtonData());
		this.addFontButtonListeners(this.printerTextFontButton,this.printerTextFontData);
		
		showLabel(composite,SWT.FILL,SWT.CENTER,SWT.LEFT | SWT.WRAP,SWT.NORMAL,0,TuxGuitar.getProperty("settings.config.styles.font.time-signature"));
		this.printerTSFontButton = new Button(composite, SWT.PUSH);
		this.printerTSFontButton.setLayoutData(makeButtonData());
		this.addFontButtonListeners(this.printerTSFontButton,this.printerTSFontData);
		
		this.loadConfig();
	}
	
	private void addFontButtonListeners(final Button button, final FontData fontData){
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				if(StylesOption.this.initialized){
					Font font = new Font(getDisplay(),fontData);
					FontData[] fontDataList = font.getFontData();
					font.dispose();
					FontDialog fontDialog = new FontDialog(getShell());
					fontDialog.setFontList(fontDataList);
					FontData result = fontDialog.open();
					if(result != null){
						loadFontData(result, fontData,button);
					}
				}
			}
		});
	}
	
	private void addColorButtonListeners(final Button button, final RGB rgb){
		button.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				Color color = new Color(getDisplay(), rgb);
				TGPainter painter = new TGPainter(e.gc);
				painter.setBackground(color);
				painter.initPath(TGPainter.PATH_FILL);
				painter.addRectangle(5,5,button.getSize().x - 10,button.getSize().y - 10);
				painter.closePath();
				color.dispose();
			}
		});
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if(StylesOption.this.initialized){
					ColorDialog dlg = new ColorDialog(getShell());
					dlg.setRGB(rgb);
					dlg.setText(TuxGuitar.getProperty("choose-color"));
					RGB result = dlg.open();
					if (result != null) {
						loadColor(result, rgb, button);
					}
				}
			}
		});
	}
	
	protected void loadFontData(FontData src, FontData dst, Button button){
		copyFontData(src, dst);
		setButtonFontData(button, dst);
	}
	
	protected void loadColor(RGB src, RGB dst, Button button){
		copyRGB(src, dst);
		button.redraw();
	}
	
	protected void setButtonFontData(Button button,FontData fontData) {
		String text = fontData.getName();
		if( (fontData.getStyle() & SWT.BOLD) != 0 ){
			text += " Bold";
		}
		if( (fontData.getStyle() & SWT.ITALIC) != 0 ){
			text += " Italic";
		}
		text += (" " + fontData.getHeight());
		button.setText(text);
	}
	
	protected void copyFontData(FontData src, FontData dst){
		dst.setName( src.getName() );
		dst.setStyle( src.getStyle() );
		dst.setHeight( src.getHeight() );
	}
	
	protected void copyRGB(RGB src, RGB dst){
		dst.red = src.red;
		dst.green = src.green;
		dst.blue = src.blue;
	}
	
	protected void loadConfig(){
		new Thread(new Runnable() {
			public void run() {
				final FontData defaultFontData = getConfig().getFontDataConfigValue(TGConfigKeys.FONT_DEFAULT);
				final FontData noteFontData = getConfig().getFontDataConfigValue(TGConfigKeys.FONT_NOTE);
				final FontData timeSignatureFontData = getConfig().getFontDataConfigValue(TGConfigKeys.FONT_TIME_SIGNATURE);
				final FontData textFontData = getConfig().getFontDataConfigValue(TGConfigKeys.FONT_TEXT);
				final FontData lyricFontData = getConfig().getFontDataConfigValue(TGConfigKeys.FONT_LYRIC);
				final FontData printerDefaultFontData = getConfig().getFontDataConfigValue(TGConfigKeys.FONT_PRINTER_DEFAULT);
				final FontData printerNoteFontData = getConfig().getFontDataConfigValue(TGConfigKeys.FONT_PRINTER_NOTE);
				final FontData printerTSFontData = getConfig().getFontDataConfigValue(TGConfigKeys.FONT_PRINTER_TIME_SIGNATURE);
				final FontData printerTextFontData = getConfig().getFontDataConfigValue(TGConfigKeys.FONT_PRINTER_TEXT);
				final FontData printerLyricFontData = getConfig().getFontDataConfigValue(TGConfigKeys.FONT_PRINTER_LYRIC);
				final RGB scoreNoteRGB  = getConfig().getRGBConfigValue(TGConfigKeys.COLOR_SCORE_NOTE);
				final RGB tabNoteRGB  = getConfig().getRGBConfigValue(TGConfigKeys.COLOR_TAB_NOTE);
				final RGB playNoteRGB  = getConfig().getRGBConfigValue(TGConfigKeys.COLOR_PLAY_NOTE);
				final RGB linesRGB  = getConfig().getRGBConfigValue(TGConfigKeys.COLOR_LINE);
				new SyncThread(new Runnable() {
					public void run() {
						if(!isDisposed()){
							loadFontData(defaultFontData,StylesOption.this.defaultFontData,StylesOption.this.defaultFontButton);
							loadFontData(noteFontData,StylesOption.this.noteFontData,StylesOption.this.noteFontButton);
							loadFontData(timeSignatureFontData,StylesOption.this.timeSignatureFontData,StylesOption.this.timeSignatureFontButton);
							loadFontData(textFontData,StylesOption.this.textFontData,StylesOption.this.textFontButton);
							loadFontData(lyricFontData,StylesOption.this.lyricFontData,StylesOption.this.lyricFontButton);
							loadFontData(printerDefaultFontData,StylesOption.this.printerDefaultFontData,StylesOption.this.printerDefaultFontButton);
							loadFontData(printerNoteFontData,StylesOption.this.printerNoteFontData,StylesOption.this.printerNoteFontButton);
							loadFontData(printerTSFontData,StylesOption.this.printerTSFontData,StylesOption.this.printerTSFontButton);
							loadFontData(printerTextFontData,StylesOption.this.printerTextFontData,StylesOption.this.printerTextFontButton);
							loadFontData(printerLyricFontData,StylesOption.this.printerLyricFontData,StylesOption.this.printerLyricFontButton);
							loadColor(scoreNoteRGB, StylesOption.this.scoreNoteRGB, StylesOption.this.scoreNoteColorButton);
							loadColor(tabNoteRGB, StylesOption.this.tabNoteRGB, StylesOption.this.tabNoteColorButton);
							loadColor(playNoteRGB, StylesOption.this.playNoteRGB, StylesOption.this.playNoteColorButton);
							loadColor(linesRGB, StylesOption.this.linesRGB, StylesOption.this.linesColorButton);
							
							StylesOption.this.initialized = true;
							StylesOption.this.pack();
						}
					}
				}).start();
			}
		}).start();
	}
	
	public GridData makeButtonData(){
		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, true);
		data.minimumWidth = BUTTON_WIDTH;
		data.minimumHeight = BUTTON_HEIGHT;
		return data;
	}
	
	public void updateConfig(){
		if(this.initialized){
			getConfig().setProperty(TGConfigKeys.FONT_DEFAULT,this.defaultFontData);
			getConfig().setProperty(TGConfigKeys.FONT_NOTE,this.noteFontData);
			getConfig().setProperty(TGConfigKeys.FONT_TIME_SIGNATURE,this.timeSignatureFontData);
			getConfig().setProperty(TGConfigKeys.FONT_TEXT,this.textFontData);
			getConfig().setProperty(TGConfigKeys.FONT_LYRIC,this.lyricFontData);
			getConfig().setProperty(TGConfigKeys.FONT_PRINTER_DEFAULT,this.printerDefaultFontData);
			getConfig().setProperty(TGConfigKeys.FONT_PRINTER_NOTE,this.printerNoteFontData);
			getConfig().setProperty(TGConfigKeys.FONT_PRINTER_TIME_SIGNATURE,this.printerTSFontData);
			getConfig().setProperty(TGConfigKeys.FONT_PRINTER_TEXT,this.printerTextFontData);
			getConfig().setProperty(TGConfigKeys.FONT_PRINTER_LYRIC,this.printerLyricFontData);
			getConfig().setProperty(TGConfigKeys.COLOR_SCORE_NOTE,this.scoreNoteRGB);
			getConfig().setProperty(TGConfigKeys.COLOR_TAB_NOTE,this.tabNoteRGB);
			getConfig().setProperty(TGConfigKeys.COLOR_PLAY_NOTE,this.playNoteRGB);
			getConfig().setProperty(TGConfigKeys.COLOR_LINE,this.linesRGB);
		}
	}
	
	public void updateDefaults(){
		if(this.initialized){
			getConfig().setProperty(TGConfigKeys.FONT_DEFAULT,getDefaults().getProperty(TGConfigKeys.FONT_DEFAULT));
			getConfig().setProperty(TGConfigKeys.FONT_NOTE,getDefaults().getProperty(TGConfigKeys.FONT_NOTE));
			getConfig().setProperty(TGConfigKeys.FONT_TIME_SIGNATURE,getDefaults().getProperty(TGConfigKeys.FONT_TIME_SIGNATURE));
			getConfig().setProperty(TGConfigKeys.FONT_TEXT,getDefaults().getProperty(TGConfigKeys.FONT_TEXT));
			getConfig().setProperty(TGConfigKeys.FONT_LYRIC,getDefaults().getProperty(TGConfigKeys.FONT_LYRIC));
			getConfig().setProperty(TGConfigKeys.FONT_PRINTER_DEFAULT,getDefaults().getProperty(TGConfigKeys.FONT_PRINTER_DEFAULT));
			getConfig().setProperty(TGConfigKeys.FONT_PRINTER_NOTE,getDefaults().getProperty(TGConfigKeys.FONT_PRINTER_NOTE));
			getConfig().setProperty(TGConfigKeys.FONT_PRINTER_TIME_SIGNATURE,getDefaults().getProperty(TGConfigKeys.FONT_PRINTER_TIME_SIGNATURE));
			getConfig().setProperty(TGConfigKeys.FONT_PRINTER_TEXT,getDefaults().getProperty(TGConfigKeys.FONT_PRINTER_TEXT));
			getConfig().setProperty(TGConfigKeys.FONT_PRINTER_LYRIC,getDefaults().getProperty(TGConfigKeys.FONT_PRINTER_LYRIC));
			getConfig().setProperty(TGConfigKeys.COLOR_SCORE_NOTE,getDefaults().getProperty(TGConfigKeys.COLOR_SCORE_NOTE));
			getConfig().setProperty(TGConfigKeys.COLOR_TAB_NOTE,getDefaults().getProperty(TGConfigKeys.COLOR_TAB_NOTE));
			getConfig().setProperty(TGConfigKeys.COLOR_PLAY_NOTE,getDefaults().getProperty(TGConfigKeys.COLOR_PLAY_NOTE));
			getConfig().setProperty(TGConfigKeys.COLOR_LINE,getDefaults().getProperty(TGConfigKeys.COLOR_LINE));
		}
	}
	
	public void applyConfig(boolean force){
		if(force || this.initialized){
			addSyncThread(new Runnable() {
				public void run() {
					TuxGuitar.instance().loadStyles();
				}
			});
		}
	}
}
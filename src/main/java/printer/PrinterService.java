package printer;

public class PrinterService {

   private static final String CARRIAGE_RETURN = System.getProperty("line.separator");

   private final Printable printer;

   public PrinterService(Printable printer){
      this.printer = printer;
      open();
   }

   public void print(String text) {
      write(text.getBytes());
   }

   public void printLn(String text) {
      print(text + CARRIAGE_RETURN);
   }

   public void lineBreak() {
      lineBreak(1);
   }

   public void lineBreak(int nbLine) {
      for (int i=0;i<nbLine;i++) {
         write(Commands.CTL_LF);
      }
   }

   public void setTextSizeNormal(){
      setTextSize(1,1);
   }

   public void setTextSize2H(){
      setTextSize(1,2);
   }

   public void setTextSize2W(){
      setTextSize(2,1);
   }

   public void setText4Square(){
      setTextSize(2,2);
   }

   private void setTextSize(int width, int height){
      if (height == 2 && width == 2) {
         write(Commands.TXT_NORMAL);
         write(Commands.TXT_4SQUARE);
      }else if(height == 2) {
         write(Commands.TXT_NORMAL);
         write(Commands.TXT_2HEIGHT);
      }else if(width == 2){
         write(Commands.TXT_NORMAL);
         write(Commands.TXT_2WIDTH);
      }else{
         write(Commands.TXT_NORMAL);
      }
   }

   public void setTextTypeBold(){
      setTextType("B");
   }

   public void setTextTypeUnderline(){
      setTextType("U");
   }

   public void setTextType2Underline(){
      setTextType("U2");
   }

   public void setTextTypeBoldUnderline(){
      setTextType("BU");
   }

   public void setTextTypeBold2Underline(){
      setTextType("BU2");
   }

   public void setTextTypeNormal(){
      setTextType("NORMAL");
   }

   private void setTextType(String type){
      if (type.equalsIgnoreCase("B")){
         write(Commands.TXT_BOLD_ON);
         write(Commands.TXT_UNDERL_OFF);
      }else if(type.equalsIgnoreCase("U")){
         write(Commands.TXT_BOLD_OFF);
         write(Commands.TXT_UNDERL_ON);
      }else if(type.equalsIgnoreCase("U2")){
         write(Commands.TXT_BOLD_OFF);
         write(Commands.TXT_UNDERL2_ON);
      }else if(type.equalsIgnoreCase("BU")){
         write(Commands.TXT_BOLD_ON);
         write(Commands.TXT_UNDERL_ON);
      }else if(type.equalsIgnoreCase("BU2")){
         write(Commands.TXT_BOLD_ON);
         write(Commands.TXT_UNDERL2_ON);
      }else if(type.equalsIgnoreCase("NORMAL")){
         write(Commands.TXT_BOLD_OFF);
         write(Commands.TXT_UNDERL_OFF);
      }
   }

   public void cutPart(){
      cut("PART");
   }

   public void cutFull(){
      cut("FULL");
   }

   private void cut(String mode){
      for (int i = 0; i < 5; i++){
         write(Commands.CTL_LF);
      }
      if (mode.toUpperCase().equals("PART")){
         write(Commands.PAPER_PART_CUT);
      }else{
         write(Commands.PAPER_FULL_CUT);
      }
   }

   public void setTextFontA(){
      setTextFont("A");
   }

   public void setTextFontB(){
      setTextFont("B");
   }

   private void setTextFont(String font){
      if (font.equalsIgnoreCase("B")){
         write(Commands.TXT_FONT_B);
      }else{
         write(Commands.TXT_FONT_A);
      }
   }

   public void setTextAlignCenter(){
      setTextAlign("CENTER");
   }

   public void setTextAlignRight(){
      setTextAlign("RIGHT");
   }

   public void setTextAlignLeft(){
      setTextAlign("LEFT");
   }

   private void setTextAlign(String align){
      if (align.equalsIgnoreCase("CENTER")){
         write(Commands.TXT_ALIGN_CT);
      }else if( align.equalsIgnoreCase("RIGHT")){
         write(Commands.TXT_ALIGN_RT);
      }else{
         write(Commands.TXT_ALIGN_LT);
      }
   }

   public void setTextDensity(int density){
      switch (density){
         case 0:
            write(Commands.PD_N50);
            break;
         case 1:
            write(Commands.PD_N37);
            break;
         case 2:
            write(Commands.PD_N25);
            break;
         case 3:
            write(Commands.PD_N12);
            break;
         case 4:
            write(Commands.PD_0);
            break;
         case 5:
            write(Commands.PD_P12);
            break;
         case 6:
            write(Commands.PD_P25);
            break;
         case 7:
            write(Commands.PD_P37);
            break;
         case 8:
            write(Commands.PD_P50);
            break;
      }
   }

   public void setTextNormal(){
      setTextProperties("LEFT", "A", "NORMAL", 1,1,9);
   }

   public void setTextProperties(String align, String font, String type, int width, int height, int density){
      setTextAlign(align);
      setTextFont(font);
      setTextType(type);
      setTextSize(width, height);
      setTextDensity(density);
   }

   public void setCharCode(String code)  {
      switch (code){
         case "USA":
            write(Commands.CHARCODE_PC437);
            break;
         case "JIS":
            write(Commands.CHARCODE_JIS);
            break;
         case "MULTILINGUAL":
            write(Commands.CHARCODE_PC850);
            break;
         case "PORTUGUESE":
            write(Commands.CHARCODE_PC860);
            break;
         case "CA_FRENCH":
            write(Commands.CHARCODE_PC863);
            break;
         default: case "NORDIC":
            write(Commands.CHARCODE_PC865);
            break;
         case "WEST_EUROPE":
            write(Commands.CHARCODE_WEU);
            break;
         case "GREEK":
            write(Commands.CHARCODE_GREEK);
            break;
         case "HEBREW":
            write(Commands.CHARCODE_HEBREW);
            break;
         case "WPC1252":
            write(Commands.CHARCODE_PC1252);
            break;
         case "CIRILLIC2":
            write(Commands.CHARCODE_PC866);
            break;
         case "LATIN2":
            write(Commands.CHARCODE_PC852);
            break;
         case "EURO":
            write(Commands.CHARCODE_PC858);
            break;
         case "THAI42":
            write(Commands.CHARCODE_THAI42);
            break;
         case "THAI11":
            write(Commands.CHARCODE_THAI11);
            break;
         case "THAI13":
            write(Commands.CHARCODE_THAI13);
            break;
         case "THAI14":
            write(Commands.CHARCODE_THAI14);
            break;
         case "THAI16":
            write(Commands.CHARCODE_THAI16);
            break;
         case "THAI17":
            write(Commands.CHARCODE_THAI17);
            break;
         case "THAI18":
            write(Commands.CHARCODE_THAI18);
            break;
      }
   }

   public void init(){
      write(Commands.HW_INIT);
   }

   public void openCashDrawerPin2() {
      write(Commands.CD_KICK_2);
   }

   public void openCashDrawerPin5() {
      write(Commands.CD_KICK_5);
   }

   public void open(){
      printer.open();
   }

   public void close(){
      printer.close();
   }

   public void beep(){
      write(Commands.BEEPER);
   }

   public void write(byte[] command){
      printer.write(command);
   }
}
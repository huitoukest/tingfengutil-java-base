# ���
tingfengutil-java-base����tingfengutil���߼����е�java���߰���������baseΪ������ʾ��ֻ������jdk�Ļ������߰���


# ���ṹ������

## common��

### exception
- ReturnException
  ���з���ֵ���쳣
  
### helper 
- PropertyHelper
  ��ȡProperty�����ļ�
- PoolHelper
  �����/��Դ�ص�ͨ�óش�����
    
### utils

#### datetime
- DateUtils ���ڴ���ʱ�䴦����

#### reflect ���乤�߰�
- ClassUtil �๤��
- GenericsUtils ���͹���
- ReflectJudgeUtils �����жϹ���
- ReflectUtils ���乤��

#### string �ַ���������
- CharSetUtil ���봦����
- StringConvertUtils �ַ���ת������
- StringUtils �ַ��������жϣ�������

### verificationCode ��֤���
- VerificationCodeUtil ���ɼ򵥵���֤��ͼƬ����Ϣ

### ����

- ArrayUtils.java ���鹤����
- BeanUtils ������
- CollectionUtils ���Ϲ�����
- CurrencyUtils ���ҹ�����
- Encrypt ���ܽ��ܹ�����
- EnumUtils ö�ٹ�����
- HtmlUtils html��������
- ObjectTypeUtils ���������жϴ�������
- ObjectUtils ����������
- ProxyByJDKUtils jdk��������
- RandomUtils �����������
- StreamUtils  ������������
- TreeUtils list��tree�ṹ��ת������
- UdpGetClientMacAddr ��ȡ����mac ip����Ϣ������

## database ��


## file��
- FileUtils
 �ļ�������
-  CSVUtil
 ��дcsv��ʽ�ļ�
 
 
## Serialization��

## web��



# Լ��
- ����jdk1.8����
- ��UTF-8��Ϊ����ʹ洢ʱ�ı���
- �����������򣬾�̬�����Ĺ�������Utils/Util(�Ƽ�)Ϊ���ƽ�β����SpringUtils.java��
- ʵ�������Ĺ�������HelperΪ���ƽ�β����SpringHelper.java(������췽���޲�������һ����spring����ע���ģʽ���п�����xxxmanage.java����)
- ���нӿ�������β�Դ�д��ĸIΪ���ƿ�ͷ(�Ƽ�)���β����BaseServiceI.java;
- ��common�о���дһЩö�٣��ӿڣ������࣬���������дʵ�ַ���������������չ��
- ������ȱ�д�����������ǣ���ǰʹ�õ�+��������������FieldSerializationProperty-->������Field�����������л���������
- ��������
��Aת��ΪB�ķ���:AUtils.toB(XXX);
��B�õ�A�ķ���:AUtils.getAbyB(XXX);����AUtils.getA(XXX);
toString,toInteger�ȳ��� �ͷ��Լ���ǰ��Ŀ����д��A����
�����õĻ���ͬһ��Ŀ�е�ʵ�ַ���,������д��B����.


package com.jzby.jzbysounderclient.util;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.text.style.ForegroundColorSpan;

public class Tools
{

	public static String longToIP(long paramLong) {
		StringBuffer localStringBuffer = new StringBuffer("");
		localStringBuffer.append(String.valueOf(paramLong & 0xFF));
		localStringBuffer.append(".");
		localStringBuffer.append(String.valueOf(0xFF & paramLong >> 8));
		localStringBuffer.append(".");
		localStringBuffer.append(String.valueOf(0xFF & paramLong >> 16));
		localStringBuffer.append(".");
		localStringBuffer.append(String.valueOf(0xFF & paramLong >> 24));
		return localStringBuffer.toString();
	}


	/**
	 * 获取替换字符串
	 * 
	 * @param data
	 *            字符串原型
	 * @param args
	 *            要替换的参数
	 * @return 替换后的字符串
	 */
	public static Spanned getReleaseString(String data, Object[] args)
	{
		String s = String.format(data, args);
		Spanned span = Html.fromHtml(s);
		return span;
	}

	/**
	 * 替换字符串某一段的颜色
	 * 
	 * @param data
	 *            数据源
	 * @param color
	 *            要替换成的颜色
	 * @param index
	 *            开始下标
	 * @param length
	 *            要替换的长度
	 * @return 替换后的字符串
	 */
	public static SpannableString getReleaseColorString(String data, int color, int index, int length)
	{
		SpannableString ss = new SpannableString(data);
		ss.setSpan(new ForegroundColorSpan(color), index, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		return ss;
	}

	/**
	 * 判断输入的电话号码格式是否正确
	 * 
	 * @param phoneNumber
	 *            字符串
	 * @return
	 */
	public static boolean accountNumberIsTrue(String phoneNumber)
	{
		String regex = "^1[3|4|5|8]\\d{9}$";
		boolean result = phoneNumber.matches(regex);
		return result;
	}

	/**
	 * 获取用户手机号码
	 * 
	 * @param context
	 *            上下文
	 * @return
	 */
	public static String getUserPhoneNumber(Context context)
	{
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String phoneNumber = telephonyManager.getLine1Number();
		if (!TextUtils.isEmpty(phoneNumber) && phoneNumber.length() > 11)
		{
			phoneNumber = phoneNumber.substring(phoneNumber.length() - 11, phoneNumber.length());
		}
		return phoneNumber;
	}

	/**
	 * 判断是否连接GPS
	 * 
	 * @param context
	 *            上下文
	 * @return
	 */
	public static boolean isOpenGps(Context context)
	{
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	/**
	 * 判断是否连接网络
	 * 
	 * @param context
	 *            上下文
	 * @return 是否连接网络
	 */
	public static boolean isConnectNet(Context context)
	{
		ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (conManager == null)
		{
			return false;
		}
		else
		{
			NetworkInfo[] info = conManager.getAllNetworkInfo();
			if (info != null)
			{
				for (int i = 0; i < info.length; i++)
				{
					if (info[i].isConnected())
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 是否存在SDCard
	 * 
	 * @return
	 */
	public static boolean isHasSDCard()
	{
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}


	/**
	 * 字符串是否是数字组成
	 * 
	 * @param str
	 *            字符串
	 * @return
	 */
	public static boolean isNumeric(String str)
	{
		Pattern pattern = Pattern.compile("[0-9]*");
		return pattern.matcher(str).matches();
	}



	/**
	 * 通过Url解析文件名字
	 * 
	 * @param imageUrl
	 *            图片uri
	 * @return 文件名字
	 */
	public static String parserImageName(String imageUrl)
	{
		try
		{
			return imageUrl.substring(imageUrl.lastIndexOf("/") + 1, imageUrl.lastIndexOf("."));
		} catch (Exception e)
		{
			e.printStackTrace();
			return imageUrl;
		}
	}

	/**
	 * 格式化文件大小
	 * 
	 * @param context
	 *            上下文
	 * 
	 * @param length
	 *            长度
	 * @return
	 */
	public static String formatFileSize(Context context, long length)
	{
		return Formatter.formatFileSize(context, length);
	}

	/**
	 * 获取图片库中选择图片的地址
	 * 
	 * @param context
	 *            上下文
	 * @param data
	 *            Intent对象
	 * @return 图片地址
	 */
	public static String getPicPathFormLibs(Context context, Intent data)
	{
		if (data != null)
		{
			Uri uri = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			Cursor cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
			if (cursor != null)
			{
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String picturePath = cursor.getString(columnIndex);
				cursor.close();
				return picturePath;
			}
			else
			{
				return null;
			}
		}
		return null;
	}

	/**
	 * 输入流转化成字节数组
	 * 
	 * @param is
	 * @return
	 */
	public static byte[] streamToBytes(InputStream is)
	{
		byte[] b = null;
		ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
		byte[] buffer = new byte[1024];
		int len;
		try
		{
			while ((len = is.read(buffer)) >= 0)
			{
				os.write(buffer, 0, len);
			}
			b = os.toByteArray();
			os.flush();
			os.close();
			is.close();
		} catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
		return b;
	}

	/**
	 * 压缩图片
	 * 
	 * @param bitmap
	 *            图片对象
	 * @param rect
	 *            压缩的尺寸
	 * @param isMax
	 *            是否是最长边
	 * @param isZoomOut
	 *            是否放大
	 * @return
	 */
	public static Bitmap resizeImage(Bitmap bitmap, int rect, boolean isMax, boolean isZoomOut)
	{
		try
		{
			// load the origial Bitmap

			int width = bitmap.getWidth();

			int height = bitmap.getHeight();

			if (!isZoomOut && rect >= width && rect >= height)
			{
				return bitmap;
			}
			int newWidth = 0;
			int newHeight = 0;

			if (isMax || isZoomOut)
			{
				newWidth = width >= height ? rect : rect * width / height;
				newHeight = width <= height ? rect : rect * height / width;
			}
			else
			{
				newWidth = width <= height ? rect : rect * width / height;
				newHeight = width >= height ? rect : rect * height / width;
			}

			if (width >= height)
			{
				if (isMax)
				{
					newWidth = rect;
					newHeight = height * newWidth / width;
				}
				else
				{
					if (isZoomOut)
					{
						newWidth = rect;
						newHeight = height * newWidth / width;
					}
					else
					{
						newHeight = rect;
						newWidth = width * newHeight / height;
					}
				}
			}
			else
			{
				if (!isMax)
				{
					newWidth = rect;
					newHeight = height * newWidth / width;
				}
				else
				{
					newHeight = rect;
					newWidth = width * newHeight / height;
				}
			}

			// calculate the scale
			float scaleWidth = 0f;
			float scaleHeight = 0f;

			scaleWidth = ((float) newWidth) / width;

			scaleHeight = ((float) newHeight) / height;

			Matrix matrix = new Matrix();

			matrix.postScale(scaleWidth, scaleHeight);

			bitmap = Bitmap.createBitmap(bitmap, 0, 0, width,

			height, matrix, true);
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}

		return bitmap;

	}

	/**
	 * 压缩图片的size
	 * 
	 * @param
	 *
	 * @param size
	 *            尺寸
	 * @return
	 * @throws IOException
	 */
	public static byte[] revitionImageSize(Bitmap bitmap, int maxRect, int size)
	{
		byte[] b = null;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try
		{
			bitmap = resizeImage(bitmap, maxRect, true, false);

			if (bitmap == null)
			{
				return null;
			}

			bitmap.compress(Bitmap.CompressFormat.JPEG, 90, os);
			b = os.toByteArray();
			int options = 80;
			while (b.length > size)
			{
				os.reset();
				bitmap.compress(Bitmap.CompressFormat.JPEG, options, os);
				b = os.toByteArray();
				options -= 10;
			}
			os.flush();
			os.close();
			bitmap = BitmapFactory.decodeByteArray(new byte[0], 0, 0);
		} catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
		return b;
	}

	/**
	 * 压缩图片的size
	 * 
	 * @param
	 *
	 * @param size
	 *            尺寸
	 * @return
	 * @throws IOException
	 */
	public static Bitmap revitionBitmap(Bitmap bitmap, int maxRect, int size)
	{
		byte[] b = null;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try
		{
			bitmap = resizeImage(bitmap, maxRect, true, false);

			if (bitmap == null)
			{
				return null;
			}

			bitmap.compress(Bitmap.CompressFormat.JPEG, 90, os);
			b = os.toByteArray();
			int options = 80;
			while (b.length > size)
			{
				os.reset();
				bitmap.compress(Bitmap.CompressFormat.JPEG, options, os);
				b = os.toByteArray();
				options -= 10;
			}
			os.flush();
			os.close();

		} catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
		return bitmap;
	}

	/**
	 * 获取图像
	 * 
	 * @param filePath
	 *            本地图片地址
	 * @param rect
	 *            图片尺寸
	 * @param isMax
	 *            是否是最长边
	 * @return
	 */
	public static Bitmap getBitmap(String filePath, int rect, boolean isMax)
	{
		return getBitmap(filePath, rect, isMax, false);
	}

	/**
	 * 获取图像
	 * 
	 * @param filePath
	 *            本地图片地址
	 * @param rect
	 *            图片尺寸
	 * @param isMax
	 *            是否是最长边
	 * @param isZoomOut
	 *            是否放大
	 * @return
	 */
	public static Bitmap getBitmap(String filePath, int rect, boolean isMax, boolean isZoomOut)
	{
		InputStream is = null;
		Bitmap photo = null;
		try
		{
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(filePath, opts);

			// 得到图片原始宽高
			int photoWidth = opts.outWidth;
			int photoHeight = opts.outHeight;

			// 判断图片是否需要缩放
			is = new FileInputStream(filePath);
			opts = new BitmapFactory.Options();

			if (photoWidth > rect || photoHeight > rect)
			{
				if (photoWidth > photoHeight)
				{
					if (isZoomOut)
					{
						opts.inSampleSize = photoWidth / rect;
					}
					else
					{
						opts.inSampleSize = isMax ? photoWidth / rect : photoHeight / rect;
					}
				}
				else
				{
					opts.inSampleSize = !isMax ? photoWidth / rect : photoHeight / rect;
				}
			}
			photo = BitmapFactory.decodeStream(is, null, opts);
			photo = rotaingImageView(Tools.readPictureDegree(filePath), photo);
			photo = resizeImage(photo, rect, isMax, isZoomOut);
			// System.out.println("压缩后图片的宽度：" + photo.getWidth());
			// System.out.println("压缩后图片的高度：" + photo.getHeight());
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}

		return photo;
	}

	/**
	 * 获得圆角图片
	 * 
	 * @param bitmap
	 *            图片
	 * @param roundPx
	 *            角度
	 * @return
	 */
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx)
	{
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Bitmap output = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, w, h);
		final RectF rectF = new RectF(rect);
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	/**
	 * 获取圆形图片对象
	 * 
	 * @param source
	 *            资源图片
	 * @return
	 */
	public static Bitmap getCircleBitmap(Bitmap source)
	{
		if (source == null)
		{
			return null;
		}

		final Paint paint = new Paint();
		paint.setAntiAlias(true);
		int sourceWidth = source.getWidth();
		int sourceHeight = source.getHeight();
		int min = Math.min(sourceWidth, sourceHeight);

		Bitmap target = Bitmap.createBitmap(min, min, Config.ARGB_8888);
		/**
		 * 产生一个同样大小的画布
		 */
		Canvas canvas = new Canvas(target);
		/**
		 * 首先绘制圆形
		 */
		canvas.drawCircle(min / 2, min / 2, min / 2, paint);
		/**
		 * 使用SRC_IN
		 */
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

		/**
		 * 绘制图片
		 */
		// canvas.drawBitmap(source, (sourceWidth - min) / 2,
		// (sourceHeight - min) / 2, paint);
		canvas.drawBitmap(source, 0, 0, paint);
		return target;
	}

	/**
	 * 旋转图片
	 * 
	 * @param angle
	 * @param bitmap
	 * @return Bitmap
	 */
	public static Bitmap rotaingImageView(int angle, Bitmap bitmap)
	{
		if (angle == 0 || bitmap == null)
		{
			return bitmap;
		}
		// 旋转图片 动作
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		// 创建新的图片
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		return resizedBitmap;
	}

	/**
	 * 读取图片属性：旋转的角度
	 * 
	 * @param path
	 *            图片绝对路径
	 * @return degree旋转的角度
	 */
	public static int readPictureDegree(String path)
	{
		int degree = 0;
		try
		{
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			switch (orientation)
			{
				case ExifInterface.ORIENTATION_ROTATE_90:
					degree = 90;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					degree = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					degree = 270;
					break;
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return degree;
	}
}

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Demo {
    //带有X幂次且系数不为1
    static final Pattern EXISTS_X = Pattern.compile("(.*[\\d+])(x\\^)(\\d+)");
    //带X系数为1的
    static final Pattern NUM_EQ_ONE = Pattern.compile("(x\\^)(\\d+)");
    //替换数字用
    static final Pattern GET_X = Pattern.compile("(.*)(x\\^.*)");

    static final Pattern TEMP = Pattern.compile("(-\\d+|\\d+)(/)(\\d+)(.*)");

    static final Pattern SB = Pattern.compile(".*/\\d+");


    public static void main(String[] args) {
        int[] arr = {1, 2, 3, 4, 114514};
        List<StringBuilder> sbList = new ArrayList<>();
        for (int i = 0; i < arr.length; i++) {
            String[] test = test(i + 1, arr.length);
            String[] merge = merge(test, arr[i]);
            for (int j = 0; j < merge.length; j++) {
                sbList.add(new StringBuilder(merge[j]));
            }
        }
        List<StringBuilder> stringBuilders = sortConcatList(sbList);
        System.out.println(sbList);
        test(sbList);
    }

    static void test(List<StringBuilder> sbList) {
        int next = 1;
        StringBuilder sb = new StringBuilder();
        String[] outArr = new String[5];
        int temp = 0;
        for (int i = 0; i < sbList.size(); i++) {
            //获取第一个x的幂次
            int base = getNum(sbList.get(i));
            while (next != sbList.size() && base == getNum(sbList.get(next))) {
                next++;
            }
            int t = i;
            while (t < next) {
                if (sb.length() == 0) {
                    Matcher m1 = TEMP.matcher(sbList.get(t));
                    if (m1.matches()) {
                        Matcher m2 = TEMP.matcher(sbList.get(t + 1));
                        //上面是分母，下面是分子
                        int b = Integer.parseInt(m1.group(1));
                        int a = Integer.parseInt(m1.group(3));
                        if (m2.matches()) {
                            int d = Integer.parseInt(m2.group(1));
                            int c = Integer.parseInt(m2.group(3));
                            sb.append(b * c + a * d).append("/").append(a * c);
                            t++;
                        }
                    }
                } else {
                    Matcher m1 = TEMP.matcher(sbList.get(t));
                    if (m1.matches()) {
                        int d = Integer.parseInt(m1.group(1));
                        int c = Integer.parseInt(m1.group(3));
                        Matcher m2 = TEMP.matcher(sb);
                        if (m2.matches()) {
                            int b = Integer.parseInt(m2.group(1));
                            int a = Integer.parseInt(m2.group(3));
                            sb = new StringBuilder(sb.toString().replaceFirst(SB.pattern(), b * c + a * d + "/" + a * c));
                        }
                    }else {
                        System.out.println(sbList.get(t));
                        System.out.println("奥里给");
                    }
                }
                t++;
            }
            outArr[temp++] = sb.toString();
            sb.delete(0, sb.length());
            i = next - 1;
            next = i + 1;
        }
        System.out.println(Arrays.toString(outArr));
    }


    /**
     * 生成拉格朗日插值公式分母部分
     *
     * @param bitValue yi 对应的值
     * @param length   y的总数
     * @return 带有分母部分的List
     */
    static List<List<String>> createDenominator(int bitValue, int length) {
        //将拉格朗日插值插值展开的分母写入List
        List<List<String>> list = new ArrayList<>();
        for (int i = 1; i <= length; i++) {
            List<String> temp = new ArrayList<>();
            if (i == bitValue) continue;
            temp.add("x^1");
            temp.add(String.valueOf(-i));
            list.add(temp);
        }
        return list;
    }

    /**
     * 生成最终不化简的方程
     *
     * @param list 单项式List
     * @return 没化简的数集合
     */
    static String[] handleEquations(List<List<String>> list) {
        String[] firstHandle = new String[0];
        for (int i = 0; i < list.size(); i++) {
            List<List<String>> tempList = new ArrayList<>(2);
            if (i == 0) {
                tempList.add(list.get(0));
                tempList.add(list.get(1));
                i++;
            } else {
                //这个临时的List是拿来算从第三个化简式起步的
                List<String> t = new ArrayList<>();
                Collections.addAll(t, firstHandle);
                tempList.add(t);
                tempList.add(list.get(i));
                //多次计算重复返回，获得最终展开式子
            }
            firstHandle = getEquation(tempList);
        }
        return firstHandle;
    }


    static String[] merge(String[] resultEquations, int value) {
        if (value == 1)
            return resultEquations;
        //用于返回的数组
        String[] returnArr = new String[resultEquations.length];
        //重新提取系数
        int[] arr1 = new int[resultEquations.length];
        int[] arr2 = new int[resultEquations.length];
        //对系数乘以一个系数再次化简
        for (int i = 0; i < resultEquations.length; i++) {
            Matcher matcher = TEMP.matcher(resultEquations[i]);
            if (matcher.matches()) {
                arr1[i] = Integer.parseInt(matcher.group(1)) * value;
                arr2[i] = Integer.parseInt(matcher.group(3));
            } else {
                if (!resultEquations[i].contains("x")) {
                    arr1[i] = Integer.parseInt(resultEquations[i]) * value;
                } else {
                    arr1[i] = getCoefficient(resultEquations[i]) * value;
                }
                arr2[i] = 1;
            }
        }
        for (int i = 0; i < resultEquations.length; i++) {
            StringBuilder sb = new StringBuilder();
            int z = getMaxCommonFactor(arr1[i], arr2[i]);
            if (z < 0) z = -z;
            if (arr2[i] != 1)
                sb.append(arr1[i] / z).append('/').append(arr2[i] / z);
            else
                sb.append(arr1[i] / z);
            if (resultEquations[i].contains("/")) {
                returnArr[i] = resultEquations[i].replaceFirst(SB.pattern(), sb.toString());
            } else {
                returnArr[i] = resultEquations[i].replaceFirst("-\\d+|\\d+", sb.toString());
            }
        }
        return returnArr;
    }

    /**
     * 合并同类项
     *
     * @param resultEquations 无化简的多项式Array
     * @param bitValue        当前yi的值
     * @return 合并同类相后的Array
     */
    static String[] mergeLikeTern(String[] resultEquations, int bitValue) {
        //x存放部分拉格朗日插值公式展开的分母
        int x = 1;
        //计算X
        for (int i = 1; i <= resultEquations.length; i++) {
            if (i == bitValue) continue;
            x *= (bitValue - i);
        }
        //用于返回的数组
        String[] returnArr = new String[resultEquations.length];
        //重新提取系数
        int[] coefficientArr = new int[resultEquations.length];
        for (int i = 0; i < resultEquations.length; i++) {
            coefficientArr[i] = getCoefficient(resultEquations[i]);
        }
        StringBuilder sb = new StringBuilder();
        //对系数乘以一个系数再次化简
        for (int i = 0; i < coefficientArr.length; i++) {
            //最大公约数
            int z = getMaxCommonFactor(coefficientArr[i], x);
            if (z < 0) z = -z;
            String xFactor = getXFactor(resultEquations[i]);
            if (coefficientArr[i] % x == 0) {
                sb.append(coefficientArr[i] / z);
            } else {
                if (coefficientArr[i] < 0 && x < 0) {
                    //系数 <0 x<0 那么整体>0
                    sb.append(-coefficientArr[i] / z).append('/').append(-x / z);
                } else if (coefficientArr[i] > 0 && x > 0) {
                    //信息署 > 0 x >0 整体 > 0
                    sb.append(coefficientArr[i] / z).append('/').append(x / z);
                } else {
                    if (coefficientArr[i] > 0) {
                        //系数>0 x<0 整体<0
                        sb.append('-').append(coefficientArr[i] / z).append('/').append(-x / z);
                    } else {
                        //系数<0 x>0 整体<0
                        sb.append('-').append(-coefficientArr[i] / z).append('/').append(x / z);
                    }
                }
            }
            if (xFactor != null)
                sb.append(xFactor);
            returnArr[i] = sb.toString();
            sb.delete(0, sb.length());
        }
        return returnArr;
    }

    static String[] test(int y, int length) {
        List<List<String>> denominatorList = createDenominator(y, length);
        //获取最终的多项式
        String[] resultEquations = handleEquations(denominatorList);
        return mergeLikeTern(resultEquations, y);
    }

    /**
     * 提取x
     *
     * @param str 单项式
     * @return 幂函数
     */
    static String getXFactor(String str) {
        Matcher matcher = GET_X.matcher(str);
        if (matcher.matches()) {
            return matcher.group(2);
        }
        return null;
    }

    /**
     * 计算最大公因数
     *
     * @param x1 除数
     * @param x2 被除数
     * @return 最大公因数
     */
    static int getMaxCommonFactor(int x1, int x2) {
        int temp = x1 % x2;
        while (temp != 0) {
            x1 = x2;
            x2 = temp;
            temp = x1 % x2;
        }
        return x2;
    }

    /**
     * 将传入的拉格朗日插值公式分母部分展开成多项式
     *
     * @param list 单项式List
     * @return 多项式的数组集合
     */
    static String[] getEquation(List<List<String>> list) {
        //用于幂次map的List
        List<Map<Integer, Integer>> mapList = new ArrayList<>(list.size());
        //将字符串分割成数字
        int[] splitArr = splitCoefficient(list, mapList);
        //进行系数计算后的数组长度
        int coefficientCalculateArrLength = 1;
        for (int i = 0; i < list.size(); i++) {
            coefficientCalculateArrLength *= list.get(i).size();
        }
//        System.out.println("splitArr:" + Arrays.toString(splitArr));
        //系数计算
        int[] coefficientCalculateArr = coefficientCalculate(splitArr, coefficientCalculateArrLength);
//        System.out.println("coefficientCalculateArr:" + Arrays.toString(coefficientCalculateArr));
        //用于拼接x及幂次的存放集合
        List<StringBuilder> concatList = new ArrayList<>(coefficientCalculateArrLength);
        //拼接系数和X
        int equationArrLength = concatCoefficientXFactor(concatList, coefficientCalculateArr, mapList);
//        System.out.println("concatList:" + concatList);
        //按幂次进行排序
        List<StringBuilder> sortConcatList = sortConcatList(concatList);
//        System.out.println("sortConcatList:" + sortConcatList);
        //合并同类相
        String[] equationArr = uniteSimilarTerms(sortConcatList, equationArrLength);
//        System.out.println("equationArr:" + Arrays.toString(equationArr));
        //以方程的形式打印
//        printEquation(equationArr);
        return equationArr;
    }

    /*static void handle(List<List<String>> list) {
        //用于幂次map的List
        List<Map<Integer, Integer>> mapList = new ArrayList<>(list.size());
        //将字符串分割成数字
        int[] splitArr = splitCoefficient(list, mapList);
        //进行系数计算后的数组长度
        int coefficientCalculateArrLength = 1;
        for (int i = 0; i < list.size(); i++) {
            coefficientCalculateArrLength *= list.get(i).size();
        }
        System.out.println("splitArr:" + Arrays.toString(splitArr));
        //系数计算
        int[] coefficientCalculateArr = coefficientCalculate(splitArr, coefficientCalculateArrLength);
        System.out.println("coefficientCalculateArr:" + Arrays.toString(coefficientCalculateArr));
        //用于拼接x及幂次的存放集合
        List<StringBuilder> concatList = new ArrayList<>(coefficientCalculateArrLength);
        //拼接系数和X
        int equationArrLength = concatCoefficientXFactor(concatList, coefficientCalculateArr, mapList);
        System.out.println("concatList:" + concatList);
        //按幂次进行排序
        List<StringBuilder> sortConcatList = sortConcatList(concatList);
        System.out.println("sortConcatList:" + sortConcatList);
        //合并同类相
        String[] equationArr = uniteSimilarTerms(sortConcatList, equationArrLength);
        System.out.println("equationArr:" + Arrays.toString(equationArr));
        //以方程的形式打印
        printEquation(equationArr);
    }*/


    /**
     * 字符串List中提取系数以及获取幂次映射
     *
     * @param baseList 原始字符串的List
     * @param mapList  幂次映射存放Map
     * @return 提取系数后的数组
     */
    static int[] splitCoefficient(List<List<String>> baseList, List<Map<Integer, Integer>> mapList) {
        int arrLength = 0;
        for (int i = 0; i < baseList.size(); i++) {
            arrLength += baseList.get(i).size();
            mapList.add(new HashMap<Integer, Integer>());
        }
        int[] splitArr = new int[arrLength];
        //用于添加元素进数组
        int temp = 0;
        //嵌套循环将数组提取并放入数组中
        for (int i = 0; i < baseList.size(); i++) {
            for (int j = 0; j < baseList.get(i).size(); j++) {
                setPowerMap(j, baseList.get(i).get(j), mapList.get(i));
                splitArr[temp++] = getCoefficient(baseList.get(i).get(j));
            }
        }
        return splitArr;
    }

    /**
     * 系数计算
     *
     * @param baseArr 没有经过计算的原系数数组
     * @param length  处理后的数组长度
     * @return 经过计算处理的数组
     */
    static int[] coefficientCalculate(int[] baseArr, int length) {
        int[] coefficientCalculateArr = new int[length];
        int step = baseArr.length - 2;
        //进行相乘操作，简言之就是后一半的第一个元素开始，乘以前一半的所有元素，然后从后一半的第二个开始相乘，以此类推
        int end = length - step;
        int start = 0;
        int index = 0;
        while (end < length) {
            while (start < length - step) {
                coefficientCalculateArr[index++] = baseArr[end] * baseArr[start++];
            }
            end++;
            if (end == length) {
                break;
            }
            start = 0;
            while (start < length - step) {
                coefficientCalculateArr[index++] = baseArr[end] * baseArr[start++];
            }
            end++;
            if (end == baseArr.length) {
                break;
            }
            start = 0;
        }
        return coefficientCalculateArr;
    }

    /**
     * 拼接系数和x
     *
     * @param concatCoefficientXFactorList 用于存放拼接系数和x的List
     * @param coefficientCalculateArr      系数经过计算后的Arr
     * @param powerMapList                 存放单项此幂次视图的List
     * @return uniteSimilarTermsLength
     */
    static int concatCoefficientXFactor(List<StringBuilder> concatCoefficientXFactorList, int[] coefficientCalculateArr, List<Map<Integer, Integer>> powerMapList) {
        int temp = 0;
        //用于判断合并同类相后剩余元素个数的set集合
        Set<Integer> tempSet = new HashSet<>();
        //连接X及幂次的操作
        for (Integer o2 : powerMapList.get(1).keySet()) {
            for (Integer o1 : powerMapList.get(0).keySet()) {
                int x1 = powerMapList.get(1).get(o2);
                int x2 = powerMapList.get(0).get(o1);
                int sum = x1 + x2;
                tempSet.add(sum);
                if (sum != 0) {
                    concatCoefficientXFactorList.add(new StringBuilder(String.valueOf(coefficientCalculateArr[temp++])).append("x^").append(sum));
                } else {
                    concatCoefficientXFactorList.add(new StringBuilder(String.valueOf(coefficientCalculateArr[temp++])));
                }
            }
        }
        return tempSet.size();
    }

    /**
     * 以方程的形式打印
     *
     * @param equationArr 合并同类相后的字符串数组
     */
    static void printEquation(String[] equationArr) {
        for (int i = 0; i < equationArr.length; i++) {
            if (equationArr[i].indexOf('-') > -1) {
                System.out.print(equationArr[i]);
            } else if (i == 0) {
                System.out.print(equationArr[i]);
            } else {
                System.out.print("+" + equationArr[i]);
            }
        }
    }

    /**
     * 合并同类项
     *
     * @param sortConcatList 排序好的连接字符串集合
     * @param length         返回数组的长度
     * @return 合并同类项后的字符串数组
     */
    static String[] uniteSimilarTerms(List<StringBuilder> sortConcatList, int length) {
        int temp = 0;
        String[] outArr = new String[length];
        //中介StringBuilder
        StringBuilder sb = new StringBuilder();
        int next = 1;
        //进行合并同类项
        for (int i = 0; i < sortConcatList.size(); i++) {
            //获取第一个x的幂次
            int base = getNum(sortConcatList.get(i));
            while (next != sortConcatList.size() && base == getNum(sortConcatList.get(next))) {
                next++;
            }
            int t = i;
            while (t < next) {
                if (sb.length() == 0) {
                    sb.append(getCoefficient(sortConcatList.get(t).toString()));
                } else {
                    int sum = Integer.parseInt(sb.toString()) + getCoefficient(sortConcatList.get(t).toString());
                    sb.delete(0, sb.length());
                    sb.append(sum);
                }
                t++;
            }
            if (base != 0 && base != 1) {
                sb.append("x^").append(base);
            } else if (base == 1) {
                sb.append("x^1");
            }
            outArr[temp++] = sb.toString();
            sb.delete(0, sb.length());
            i = next - 1;
            next = i + 1;
        }
        return outArr;
    }

    /**
     * 提取字符串的x的幂次
     *
     * @param sb sb
     * @return x的幂次
     */
    static int getNum(StringBuilder sb) {
        //先判断带X且系数不为1
        Matcher m1 = EXISTS_X.matcher(sb.toString());
        if (m1.matches()) {
            return Integer.parseInt(m1.group(3));
        }
        //再判断带X系数且为1
        Matcher m2 = NUM_EQ_ONE.matcher(sb.toString());
        if (m2.matches()) {
            return 1;
        }
        //没有带X，直接返回数字
        return 0;
    }

    /**
     * 按幂次进行排序下，大的在前面，小的在后面
     *
     * @param list concatList
     * @return 幂次从大到小的List
     */
    static List<StringBuilder> sortConcatList(List<StringBuilder> list) {
        list.sort((o1, o2) -> {
            if (o1.lastIndexOf("x") > 0) {
                //有幂次进行判断
                if (o2.lastIndexOf("x") > 0) {
                    int num1 = Demo.getNum(o1);
                    int num2 = Demo.getNum(o2);
                    //幂次大的排前面
                    return num1 > num2 ? -1 : 1;
                }
                //没有幂次或者幂次小的排后面
                return -1;
            }
            //只有系数则排后面
            return 1;
        });
        return list;
    }

    /**
     * 提取x的幂次到一个map中
     *
     * @param index x存在的下标
     * @param str   被解析的字符串
     * @param map   以x下标为键，以x的幂次为值
     */
    static void setPowerMap(int index, String str, Map<Integer, Integer> map) {
        //先判断带X且系数不为1
        Matcher m1 = EXISTS_X.matcher(str);
        if (m1.matches()) {
            map.put(index, Integer.parseInt(m1.group(3)));
            return;
        }
        //再判断带X系数且为1
        Matcher m2 = NUM_EQ_ONE.matcher(str);
        if (m2.matches()) {
            map.put(index, Integer.parseInt(m2.group(2)));
            return;
        }
        map.put(index, 0);
    }

    /**
     * 提取x的系数
     *
     * @param str 被解析的字符串
     * @return 字符串提取出来的系数
     */
    static int getCoefficient(String str) {
        //先判断带X且系数不为1
        Matcher m1 = EXISTS_X.matcher(str);
        if (m1.matches()) {
            return Integer.parseInt(m1.group(1));
        }
        //再判断带X系数且为1
        Matcher m2 = NUM_EQ_ONE.matcher(str);
        if (m2.matches()) {
            return 1;
        }
        //没有带X，直接返回数字
        return Integer.parseInt(str);
    }


}







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

    static final Pattern TEMP = Pattern.compile("(.*)(/)(\\d+)(.*)");

    static final Pattern SB = Pattern.compile(".*/\\d+");

    static final Pattern TEST = Pattern.compile("(.*)(/)(.*)(x\\^.*)");

    static final Pattern AOLIGEI = Pattern.compile("(.*)(/)(.*)");

    public static void main(String[] args) {
        int[] arr = {1, 2, 3, 4, 5201314};
        List<StringBuilder> sbList = new ArrayList<>();
        //进行所有式子整合到一个List中
        for (int i = 0; i < arr.length; i++) {
            //合并同类相后没有*yi的式子
            String[] noMultiplyCoefficientEquation = test(i + 1, arr.length);
            //返回乘上系数的多项式
            String[] multiplyCoefficientEquation = multiplyCoefficient(noMultiplyCoefficientEquation, arr[i]);
            for (String s : multiplyCoefficientEquation) {
                sbList.add(new StringBuilder(s));
            }
        }
        ///按幂次排序
        List<StringBuilder> sortList = sortList(sbList);
        //合并所有方程同类项
        String[] testB = mergeAllLikeTerms(sortList, arr.length);
        //方程形式打印
        printEquation(testB);
    }

    /**
     * 带入X计算多项式
     *
     * @param multinomial 多项式
     * @param t           yi
     */
    static void testC(String[] multinomial, int t) {
        StringBuilder sum = new StringBuilder();
        String[] arr = new String[multinomial.length];
        //用于判断基数是分数还是整数,0为整数，1为分数
        int state = 0;
        //基数的power
        int basePower;
        //当前的power
        int currentPower = multinomial.length - 1;
        for (int i = 0; i < multinomial.length; i++) {
            basePower = currentPower;
            currentPower = getNum(new StringBuilder(multinomial[i]));
            if (i == 0) {
                //进行首次赋值
                if (multinomial[i].lastIndexOf("/") != -1) {
                    //分数
                    Matcher m1 = TEST.matcher(multinomial[i]);
                    if (m1.matches()) {
                        int b = Integer.parseInt(m1.group(1));
                        int a = Integer.parseInt(m1.group(3));
                        sum.append(b).append('/').append(a);
                        state = 1;
                    }
                } else {
                    //整数
                    sum.append(getCoefficient(multinomial[i]));
                }
            } else {
                if (state == 1) {
                    //基数为分数时
                    if (multinomial[i].lastIndexOf("/") != -1) {
                        //加数为分数时
                        Matcher m1 = TEST.matcher(multinomial[i]);
                        if (m1.matches()) {
                            //提取加数分子分母
                            int d = Integer.parseInt(m1.group(1));
                            d *= Math.pow(t, currentPower);
                            int c = Integer.parseInt(m1.group(3));
                            Matcher m2 = AOLIGEI.matcher(sum);
                            if (m2.matches()) {
                                //提取基数分子分母
                                int b = Integer.parseInt(m2.group(1));
                                b *= Math.pow(t, basePower);
                                int a = Integer.parseInt(m2.group(3));
                                int x1 = b * c + a * d;
                                int x2 = a * c;
                                int z = getMaxCommonFactor(x1 > 0 ? x1 : -x1, x2);
                                sum = testD(x1, x2, z);
                            }
                        }
                    } else {
                        //加数为整数时
                        Matcher m1 = AOLIGEI.matcher(sum);
                        if (m1.matches()) {
                            int b = Integer.parseInt(m1.group(1));
                            int a = Integer.parseInt(m1.group(3));
                            int d = getCoefficient(multinomial[i]);
                            b *= Math.pow(t, basePower);
                            d *= Math.pow(t, currentPower);
                            int x1 = b + a * d;
                            int z = getMaxCommonFactor(x1, a);
                            sum = testD(x1, a, z);
                        }
                    }
                } else {
                    //基数为整数时
                    if (multinomial[i].lastIndexOf("/") != -1) {
                        //加数为分数时
                        Matcher m1 = TEST.matcher(multinomial[i]);
                        if (m1.matches()) {
                            int d = Integer.parseInt(m1.group(1));
                            int c = Integer.parseInt(m1.group(3));
                            d *= Math.pow(t, currentPower);
                            int b = Integer.parseInt(sum.toString());
                            b *= Math.pow(t, basePower);
                            int x2 = b * c + d;
                            int z = getMaxCommonFactor(b, x2);
                            sum = testD(b, x2, z);
                            state = 1;
                        }
                    } else {
                        //加数为整数时
                        int b = Integer.parseInt(multinomial[i]);
                        b *= Math.pow(t, currentPower);
                        int a = Integer.parseInt(sum.toString());
                        a *= Math.pow(t, basePower);
                        sum = new StringBuilder().append(a + b);
                        state = 0;
                    }
                }
            }
            System.out.println(sum);
        }
    }

    /**
     * 化简分子分母
     *
     * @param x1 分子
     * @param x2 分母
     * @param z  最大公因数
     * @return 分数的最简形式
     */
    static StringBuilder testD(int x1, int x2, int z) {
        if (z > 0) {
            //x2 > 0
            if (x1 > 0) {
                //整体>0
                return new StringBuilder().append(x1 / z).append('-').append(x2 / z);
            } else {
                //整体<0
                return new StringBuilder().append('-').append(-x1 / z).append('-').append(x2 / z);
            }
        } else {
            //x2 < 0
            if (x1 > 0) {
                //整体<0
                return new StringBuilder().append('-').append(x1 / z).append('-').append(-x2 / z);
            } else {
                //整体>0
                return new StringBuilder().append(x1 / z).append('-').append(-x2 / z);
            }
        }
    }

    /**
     * 合并同类项
     *
     * @param sbList    排序好的未合并的多项式
     * @param outLength 输出数组长度
     * @return 合并同类项后的字符数组
     */
    static String[] mergeAllLikeTerms(List<StringBuilder> sbList, int outLength) {
        int next = 1;
        StringBuilder sb = new StringBuilder();
        String[] outArr = new String[outLength];
        int temp = 0;
        for (int i = 0; i < sbList.size(); i++) {
            //获取第一个x的幂次
            int base = getNum(sbList.get(i));
            while (next != sbList.size() && base == getNum(sbList.get(next))) {
                next++;
            }
            int t = i;
            while (t < next) {
                //sb中为空时，就是没有基数，要添加基数
                if (sb.length() == 0) {
                    //基数为分数时
                    if (sbList.get(t).lastIndexOf("/") != -1) {
                        Matcher m1 = TEMP.matcher(sbList.get(t));
                        if (m1.matches()) {
                            Matcher m2 = TEMP.matcher(sbList.get(t + 1));
                            //上面是分母，下面是分子
                            int b = Integer.parseInt(m1.group(1));
                            int a = Integer.parseInt(m1.group(3));
                            if (m2.matches()) {
                                int d = Integer.parseInt(m2.group(1));
                                int c = Integer.parseInt(m2.group(3));
                                //  b/a + d/c = bc+ad / ac
                                sb.append(b * c + a * d).append("/").append(a * c);
                                t++;
                            }
                        }
                    } else {
                        //为整数，且sb中无任何值，直接添加
                        sb.append(getCoefficient(sbList.get(t).toString()));
                    }
                }
                //sb不为空，说明有基数
                else {
                    //基数为分数时
                    if (sb.lastIndexOf("/") != -1) {
                        //加数为分数时
                        if (sbList.get(t).lastIndexOf("/") != -1) {
                            Matcher m1 = TEMP.matcher(sb);
                            if (m1.matches()) {
                                int b = Integer.parseInt(m1.group(1));
                                int a = Integer.parseInt(m1.group(3));
                                Matcher m2 = TEMP.matcher(sbList.get(t));
                                if (m2.matches()) {
                                    int d = Integer.parseInt(m2.group(1));
                                    int c = Integer.parseInt(m2.group(3));
                                    int b1 = b * c + a * d;
                                    int b2 = a * c;
                                    int z = getMaxCommonFactor(b1, b2);
                                    if (z < 0) z *= -1;
                                    sb = new StringBuilder().append(b1 / z).append('/').append(b2 / z);
                                }
                            }
                        } else {
                            //加数不是分数时
                            if (sbList.get(t).lastIndexOf("x") != -1) {
                                Matcher m1 = TEMP.matcher(sb);
                                if (m1.matches()) {
                                    int b = Integer.parseInt(m1.group(1));
                                    int a = Integer.parseInt(m1.group(3));
                                    int d = getCoefficient(sbList.get(t).toString());
                                    int b1 = b + a * d;
                                    int z = getMaxCommonFactor(b1, a);
                                    if (z < 0) z *= -1;
                                    sb = new StringBuilder().append(b1 / z).append('/').append(a / z);
                                }
                            }
                        }
                    } else {
                        //基数不是分数时且是关于x的单项式
                        if (sb.lastIndexOf("x") != -1) {
                            if (sbList.get(t).lastIndexOf("/") != -1) {
                                //加数为分数时
                                Matcher m1 = TEMP.matcher(sbList.get(t));
                                if (m1.matches()) {
                                    int b = getCoefficient(sb.toString());
                                    int d = Integer.parseInt(m1.group(1));
                                    int c = Integer.parseInt(m1.group(3));
                                    int b1 = b * c + d;
                                    int z = getMaxCommonFactor(b1, c);
                                    if (z < 0) z *= -1;
                                    sb = new StringBuilder().append(b1 / z).append('/').append(c / z);
                                }
                            } else {
                                //加数不是分数时
                                sb = new StringBuilder().append(getCoefficient(sbList.get(t).toString()) + getCoefficient(sb.toString()));
                            }
                        } else {
                            //基数不是分数时且仅为常数
                            sb = new StringBuilder().append(getCoefficient(sbList.get(t).toString()) + getCoefficient(sb.toString()));
                        }
                    }
                }
                t++;
            }
            if (base != 0) sb.append("x^").append(base);
            outArr[temp++] = sb.toString();
            sb.delete(0, sb.length());
            i = next - 1;
            next = i + 1;
        }
        return outArr;
    }


    /**
     * 生成拉格朗日插值公式分子部分
     *
     * @param bitValue yi 对应的值
     * @param length   y的总数
     * @return 带有分母部分的List
     */
    static List<List<String>> createMoleculePart(int bitValue, int length) {
        //用于返回的零时List
        List<List<String>> list = new ArrayList<>(length - 1);
        for (int i = 1; i <= length; i++) {
            if (i == bitValue) continue;
            List<String> temp = new ArrayList<>(2);
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
    static String[] unfoldBasePart(List<List<String>> list) {
        //用于输出的Arr
        String[] outArr = new String[0];
        for (int i = 0; i < list.size(); i++) {
            List<List<String>> tempList = new ArrayList<>(2);
            if (i == 0) {
                //首先提取两个式子，例如(x - 1) (x - 2)进行计算
                tempList.add(list.get(0));
                tempList.add(list.get(1));
                //这里一次性读取两个式子，要多加1
                i++;
            } else {
                //这个临时的List是拿来算从第三个化简式起步的
                List<String> t = new ArrayList<>();
                Collections.addAll(t, outArr);
                tempList.add(t);
                tempList.add(list.get(i));
                //多次计算重复返回，获得最终展开式子
            }
            //计算方程
            outArr = getEquation(tempList);
        }
        return outArr;
    }


    /**
     * 将多项式方程*yi
     *
     * @param resultEquations 多项式方程
     * @param value           yi
     * @return 乘上系数后的多项式方程
     */
    static String[] multiplyCoefficient(String[] resultEquations, int value) {
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
     * 展开式除以分母（系数）
     *
     * @param resultEquations 分子部分展开式
     * @param bitValue        当前位的值
     * @return 展开式除以分母后进行拆分的单项式集合（已经化简）
     */
    static String[] divideDenominator(String[] resultEquations, int bitValue) {
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
            //让最大公约数保证符号只与x有关
            int z = getMaxCommonFactor(coefficientArr[i] > 0 ? coefficientArr[i] : -coefficientArr[i], x);
            String xFactor = getXFactor(resultEquations[i]);
            if (coefficientArr[i] % x == 0) {
                //能整除时，不需要管正负号
                sb.append(coefficientArr[i] / z);
            } else {
                if (z < 0) {
                    z = -z;
                    //说明x<0
                    if (coefficientArr[i] > 0) {
                        //分母的常数项>0 整体<0
                        sb.append('-').append(coefficientArr[i] / z).append('/').append(-x / z);
                    } else {
                        //整体>0
                        sb.append(-coefficientArr[i] / z).append('/').append(-x / z);
                    }
                } else {
                    //说明x>0
                    if (coefficientArr[i] > 0) {
                        //分母的常数项>0 整体>0
                        sb.append(coefficientArr[i] / z).append('/').append(x / z);
                    } else {
                        //整体<0
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

    static String[] test(int bitValue, int length) {
        //生成分子部分，每一个List<String>存放对应x-i的单项式集合
        List<List<String>> denominatorList = createMoleculePart(bitValue, length);
        //将分子部分展开并合并同类项
        String[] resultEquations = unfoldBasePart(denominatorList);
        //返回展开式除以分母且合并同类项的式子
        return divideDenominator(resultEquations, bitValue);
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
        int state = 1;
        if (x2 < 0) state *= -1;
        int temp = x1 % x2;
        while (temp != 0) {
            x1 = x2;
            x2 = temp;
            temp = x1 % x2;
        }
        return Math.abs(x2) * state;
    }

    /**
     * 将传入的拉格朗日插值公式分母部分展开成多项式
     *
     * @param list 单项式List
     * @return 多项式的数组集合
     */
    static String[] getEquation(List<List<String>> list) {
        //存放每个多项式中，各个单项式的X的幂次的映射
        List<Map<Integer, Integer>> mapList = new ArrayList<>(list.size());
        //提取每个单项式的系数，并依次存放，这里还处理了幂次的映射问题
        int[] baseCoefficient = getCoefficientAndPowerMap(list, mapList);
        //每个多项式经过系数计算后的放入一个数组的总长度（两个多项式相乘，这里用系数计算，后面会拼接上未知数）
        int coefficientCalculateArrLength = 1;
        for (int i = 0; i < list.size(); i++) {
            //因为是两个式子相乘，所以总长就是每个式子的成绩
            coefficientCalculateArrLength *= list.get(i).size();
        }
        //多项式系数计算后的方程，里面都存放的都是系数，用于下一步拼接未知数x
        int[] coefficientCalculateArr = coefficientCalculate(baseCoefficient, coefficientCalculateArrLength);
        //用于存放拼接未知数x的集合
        List<StringBuilder> concatList = new ArrayList<>(coefficientCalculateArrLength);
        //拼接系数和X，返回的是合并同类项后的数组长度
        int equationArrLength = concatCoefficientXFactor(concatList, coefficientCalculateArr, mapList);
        //按幂次进行排序
        List<StringBuilder> sortConcatList = sortList(concatList);
        //合并同类项
        String[] equationArr = uniteSimilarTerms(sortConcatList, equationArrLength);
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
    static int[] getCoefficientAndPowerMap(List<List<String>> baseList, List<Map<Integer, Integer>> mapList) {
        //输出数组的大小
        int arrLength = 0;
        for (int i = 0; i < baseList.size(); i++) {
            //输出数组的大小就是每个小List的元素大小之和
            arrLength += baseList.get(i).size();
            //添加Map
            mapList.add(new HashMap<>());
        }
        //输出数组，里面的元素就是List中提取的系数，并且是按顺序的
        int[] baseCoefficient = new int[arrLength];
        //用于遍历baseCoefficient
        int temp = 0;
        for (int i = 0; i < baseList.size(); i++) {
            for (int j = 0; j < baseList.get(i).size(); j++) {
                //提取字符串，因为下面两个都用到了
                String s = baseList.get(i).get(j);
                //设置幂次映射
                setPowerMap(j, s, mapList.get(i));
                //提取系数放入输出数组中
                baseCoefficient[temp++] = getCoefficient(s);
            }
        }
        return baseCoefficient;
    }

    /**
     * 两个多项式式子计算
     *
     * @param baseCoefficient 没有经过计算的原系数数组
     * @param length          系数计算后存放数组的长度
     * @return 经过计算后的数组
     */
    static int[] coefficientCalculate(int[] baseCoefficient, int length) {
        int[] coefficientCalculateArr = new int[length];
        //计算后规定，后一项多项式只能有两项单项式，而输出的数组是合并后的，所以要减去2当，后两个就是乘数，前面的数都是被乘数
        int step = baseCoefficient.length - 2;
        //end就是第二个List的第一个系数的下标
        int end = length - step;
        //start从0开始，就是第一个List的第一个系数开始的下标
        int start = 0;
        //用于数组
        int index = 0;
        while (end < length) {
            //第二个List的第一个系数，乘上第一个List的所有系数
            while (start < length - step) {
                coefficientCalculateArr[index++] = baseCoefficient[end] * baseCoefficient[start++];
            }
            //第二个弟一个List乘完之后，开始向后移一个
            end++;
            if (end == length) {
                break;
            }
            //将第一个List的下标归零，继续相乘
            start = 0;
            while (start < length - step) {
                coefficientCalculateArr[index++] = baseCoefficient[end] * baseCoefficient[start++];
            }
            //下面以及后面以此类推
            end++;
            if (end == baseCoefficient.length) {
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
     * @return uniteSimilarTermsLength 合并同类项后数组的长度
     */
    static int concatCoefficientXFactor(List<StringBuilder> concatCoefficientXFactorList, int[] coefficientCalculateArr, List<Map<Integer, Integer>> powerMapList) {
        int temp = 0;
        //用于判断合并同类相后剩余元素个数的set集合
        Set<Integer> tempSet = new HashSet<>();
        //连接X及幂次的操作,这里只有两个Map，所以直接用数表示了
        for (Integer o2 : powerMapList.get(1).keySet()) {
            for (Integer o1 : powerMapList.get(0).keySet()) {
                //提取后面多项式的第一个幂
                int x1 = powerMapList.get(1).get(o2);
                //地区前面多项式的第一个幂
                int x2 = powerMapList.get(0).get(o1);
                //相加
                int sum = x1 + x2;
                tempSet.add(sum);
                //存放进数组中
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
            //计算到下一个幂次的下标
            while (next != sortConcatList.size() && base == getNum(sortConcatList.get(next))) {
                next++;
            }
            int t = i;
            //同幂次进行相加
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
            //如果幂次不为0时
            if (base != 0) {
                sb.append("x^").append(base);
            }
            outArr[temp++] = sb.toString();
            sb.delete(0, sb.length());
            //同幂次的计算后，将开始的下标推到下一个幂次的起始下标-1，因为for循环还有i++
            i = next - 1;
            //下一个的下一个幂次的下标开始就是i+1了，因为上面还会计算到下一个次幂的下标
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
    static List<StringBuilder> sortList(List<StringBuilder> list) {
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
     * @param index 当前的下标
     * @param str   被解析的字符串
     * @param map   以当前的下标为键，以x的幂次为值
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







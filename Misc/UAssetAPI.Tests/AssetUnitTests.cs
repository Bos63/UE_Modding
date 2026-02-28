using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using UAssetAPI.ExportTypes;
using UAssetAPI.PropertyTypes.Objects;
using UAssetAPI.PropertyTypes.Structs;
using UAssetAPI.UnrealTypes;
using UAssetAPI.Unversioned;

namespace UAssetAPI.Tests
{
    [TestClass]
    public class AssetUnitTests
    {
        /// <summary>
        /// Checks if two files have the same binary data.
        /// </summary>
        public void VerifyBinaryEquality(string file1, string file2)
        {
            int file1byte;
            int file2byte;
            FileStream fs1;
            FileStream fs2;

            if (file1 == file2) return;

            fs1 = new FileStream(file1, FileMode.Open);
            fs2 = new FileStream(file2, FileMode.Open);

            if (fs1.Length != fs2.Length)
            {
                fs1.Close();
                fs2.Close();
                Assert.IsTrue(false);
            }

            do
            {
                file1byte = fs1.ReadByte();
                file2byte = fs2.ReadByte();
            }
            while ((file1byte == file2byte) && (file1byte != -1));

            fs1.Close();
            fs2.Close();

            Assert.IsTrue((file1byte - file2byte) == 0);
        }

        /// <summary>
        /// Asserts that all exports in an asset have parsed correctly.
        /// </summary>
        /// <param name="tester">The asset to test.</param>
        public void AssertAllExportsParsedCorrectly(UAsset tester)
        {
            foreach (Export testExport in tester.Exports)
            {
                Assert.IsFalse(testExport is RawExport, $"Export '{testExport.ObjectName}' in '{tester.FilePath}' was not parsed correctly (RawExport)");
                if (testExport is FunctionExport funcExport)
                {
                    Assert.IsNotNull(funcExport.ScriptBytecode, $"FunctionExport '{testExport.ObjectName}' in '{tester.FilePath}' has null ScriptBytecode (failed to parse Kismet bytecode)");
                }
            }
        }

        /// <summary>
        /// Retrieves all the test assets in a particular folder.
        /// </summary>
        /// <param name="folder">The folder to check for test assets.</param>
        /// <returns>An array of paths to assets that should be tested.</returns>
        public string[] GetAllTestAssets(string folder)
        {
            List<string> allFilesToTest = Directory.GetFiles(folder, "*.uasset", SearchOption.AllDirectories).ToList();
            allFilesToTest.AddRange(Directory.GetFiles(folder, "*.umap", SearchOption.AllDirectories));
            return allFilesToTest.ToArray();
        }

        /// <summary>
        /// Tests <see cref="FSoftObjectPath"/> equality functionality including IEquatable implementation.
        /// </summary>
        [TestMethod]
        public void TestFSoftObjectPathEquality()
        {
            var dummyAsset = new UAsset(Path.Combine("TestAssets", "TestManyAssets", "Astroneer", "Augment_BroadBrush.uasset"), EngineVersion.VER_UE4_23);

            var packageName1 = new FName(dummyAsset, "TestPackage");
            var assetName1 = new FName(dummyAsset, "TestAsset");
            var subPath1 = new FString("SubPath1");

            var packageName2 = new FName(dummyAsset, "TestPackage");
            var assetName2 = new FName(dummyAsset, "TestAsset");
            var subPath2 = new FString("SubPath1");

            var packageName3 = new FName(dummyAsset, "DifferentPackage");
            var assetName3 = new FName(dummyAsset, "DifferentAsset");
            var subPath3 = new FString("SubPath2");

            var path1 = new FSoftObjectPath(packageName1, assetName1, subPath1);
            var path2 = new FSoftObjectPath(packageName2, assetName2, subPath2);
            var path3 = new FSoftObjectPath(packageName3, assetName3, subPath3);
            var path4 = new FSoftObjectPath(packageName1, assetName1, null);

            Assert.IsTrue(path1.Equals(path2));
            Assert.IsTrue(path2.Equals(path1));
            Assert.IsFalse(path1.Equals(path3));
            Assert.IsFalse(path1.Equals(path4));

            Assert.IsTrue(path1.Equals((object)path2));
            Assert.IsFalse(path1.Equals((object)path3));
            Assert.IsFalse(path1.Equals(null));
            Assert.IsFalse(path1.Equals("string"));

            Assert.IsTrue(path1 == path2);
            Assert.IsFalse(path1 == path3);

            Assert.IsFalse(path1 != path2);
            Assert.IsTrue(path1 != path3);

            Assert.IsTrue(path1.GetHashCode() == path2.GetHashCode());
        }

        /// <summary>
        /// Tests <see cref="FName.ToString"/> and <see cref="FName.FromString"/>.
        /// </summary>
        [TestMethod]
        public void TestNameConstruction()
        {
            var dummyAsset = new UAsset(Path.Combine("TestAssets", "TestManyAssets", "Astroneer", "Augment_BroadBrush.uasset"), EngineVersion.VER_UE4_23);

            FName test = FName.FromString(dummyAsset, "HelloWorld_0");
            Assert.IsTrue(test.Value.Value == "HelloWorld" && test.Number == 1);
            Assert.IsTrue(test.ToString() == "HelloWorld_0");

            test = new FName(dummyAsset, "HelloWorld", 2);
            Assert.IsTrue(test.ToString() == "HelloWorld_1");

            test = new FName(dummyAsset, "HelloWorld", 0);
            Assert.IsTrue(test.ToString() == "HelloWorld");
        }

        /// <summary>
        /// Tests modifying values within the class default object of an asset.
        /// </summary>
        [TestMethod]
        public void TestCDOModification()
        {
            var tester = new UAsset(Path.Combine("TestAssets", "TestManyAssets", "Astroneer", "Augment_BroadBrush.uasset"), EngineVersion.VER_UE4_23);
            Assert.IsTrue(tester.VerifyBinaryEquality());

            NormalExport cdoExport = null;
            foreach (Export testExport in tester.Exports)
            {
                if (testExport.ObjectFlags.HasFlag(EObjectFlags.RF_ClassDefaultObject))
                {
                    cdoExport = (NormalExport)testExport;
                    break;
                }
            }
            Assert.IsNotNull(cdoExport);

            cdoExport["PickupActor"] = new ObjectPropertyData() { Value = FPackageIndex.FromRawIndex(0) };

            Assert.IsTrue(cdoExport["PickupActor"] is ObjectPropertyData);
            Assert.IsTrue(((ObjectPropertyData)cdoExport["PickupActor"]).Value.Index == 0);
        }

        private void TestManyAssetsSubsection(string game, EngineVersion version, Usmap mappings = null)
        {
            string[] allTestingAssets = GetAllTestAssets(Path.Combine("TestAssets", "TestManyAssets", game));
            foreach (string assetPath in allTestingAssets)
            {
                Console.WriteLine(assetPath);
                var tester = new UAsset(assetPath, version, mappings);
                Assert.IsTrue(tester.VerifyBinaryEquality());
                AssertAllExportsParsedCorrectly(tester);
                Console.WriteLine(tester.GetEngineVersion());
            }
        }

        [TestMethod]
        public void TestManyAssets()
        {
            TestManyAssetsSubsection("Astroneer", EngineVersion.VER_UE4_23);
            TestManyAssetsSubsection("VERSIONED", EngineVersion.UNKNOWN);
        }

        [TestMethod]
        public void TestGUIDs()
        {
            string input = "{CF873D05-4977-597A-F120-7F9F90B1ED09}";
            Guid test = input.ConvertToGUID();
            Assert.IsTrue(test.ConvertToString() == input);
            Assert.IsTrue(test.ToByteArray().SequenceEqual(UAPUtils.ConvertHexStringToByteArray("05 3D 87 CF 7A 59 77 49 9F 7F 20 F1 09 ED B1 90")));
        }

        public static MemoryStream PathToStream(string p)
        {
            using (FileStream origStream = File.Open(p, FileMode.Open, new FileInfo(p).IsReadOnly ? FileAccess.Read : FileAccess.ReadWrite))
            {
                MemoryStream completeStream = new MemoryStream();
                origStream.CopyTo(completeStream);

                try
                {
                    var targetFile = Path.ChangeExtension(p, "uexp");
                    if (File.Exists(targetFile))
                    {
                        using (FileStream newStream = File.Open(targetFile, FileMode.Open))
                        {
                            completeStream.Seek(0, SeekOrigin.End);
                            newStream.CopyTo(completeStream);
                        }
                    }
                }
                catch (FileNotFoundException) { }

                completeStream.Seek(0, SeekOrigin.Begin);
                return completeStream;
            }
        }

        [AssemblyCleanup()]
        public static void AssemblyCleanup()
        {
            foreach (var path in Directory.GetDirectories("."))
            {
                if (Path.GetFileName(path).Length < 4 || Path.GetFileName(path).Substring(0, 4).ToLowerInvariant() != "test") continue;
                try
                {
                    Directory.Delete(path, true);
                }
                catch { }
            }
        }
    }
}

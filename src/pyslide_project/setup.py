from setuptools import setup

setup(
    name='pyslide',
    version='0.0.1',
    packages=[],
    package_dir={'': 'pyslide'},
    url='https://github.com/soumitag/multiSLIDE',
    license='GPLv3',
    author='Soumita Ghosh and Abhik Datta',
    author_email='',
    description='Analytical server for multiSLIDE',
    install_requires=['waitress', 'flask', 'numpy', 'pandas', 'scipy', 'pymongo', 'fastcluster']
)

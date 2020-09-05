from setuptools import setup

setup(
    name='pyslide',
    version='0.0.1',
    packages=['https://github.com/soumitag/multiSLIDE'],
    package_dir={'': 'pyslide'},
    url='',
    license='GPLv3',
    author='Soumita Ghosh and Abhik Datta',
    author_email='',
    description='Analytical server for multiSLIDE',
    install_requires=['waitress', 'flask', 'numpy', 'pandas', 'scipy', 'pymongo', 'fastcluster']
)
